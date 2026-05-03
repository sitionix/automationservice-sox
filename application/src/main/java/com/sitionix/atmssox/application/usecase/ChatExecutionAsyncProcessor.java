package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatExecutionAsyncProcessor {

    private final ChatExecutionRepository chatExecutionRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationContextSnapshotRepository conversationContextSnapshotRepository;
    private final ConversationContextBuilder conversationContextBuilder;
    private final ContextOptimizerProperties contextOptimizerProperties;
    private final AgentExecutionService agentExecutionService;
    private final PostChatWorkflowDispatcher postChatWorkflowDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatExecutionSubmitted(final ChatExecutionSubmittedEvent event) {
        this.processAsync(event.executionId());
    }

    @Async("contextOptimizerTaskExecutor")
    @Transactional
    public void processAsync(final UUID executionId) {
        log.info("[CHAT_EXECUTION] async started executionId={}", executionId);
        this.process(executionId);
    }

    public void process(final UUID executionId) {
        final ChatExecution queuedExecution = this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED)
                .orElse(null);
        if (queuedExecution == null) {
            return;
        }

        final Instant startedAt = Instant.now();
        ChatExecution execution = this.chatExecutionRepository.save(queuedExecution.toBuilder()
                .status(ChatExecutionStatus.IN_PROGRESS)
                .startedAt(startedAt)
                .build());
        log.info("[CHAT_EXECUTION] execution marked RUNNING executionId={}", execution.getExecutionId());

        try {
            final Conversation conversation = this.conversationRepository.findActiveByIdAndUserIdAndAgentId(
                    execution.getConversationId(),
                    execution.getUserId(),
                    execution.getAgentId()
            ).orElseThrow(() -> new AgentNotFoundException("Conversation not found"));

            if (conversation.getType() != ConversationType.DIRECT) {
                throw new AgentChatNotAllowedException("Conversation type is not supported by direct handler");
            }

            this.conversationParticipantRepository.findAllByConversationId(conversation.getId());
            final Agent agent = this.agentRepository.findVisibleByIdAndUserId(execution.getAgentId(), execution.getUserId())
                    .orElseThrow(() -> new AgentNotFoundException("Agent not found"));
            if (agent.getStatus() != AgentStatus.ACTIVE) {
                throw new AgentChatNotAllowedException("Only ACTIVE agent can execute chat");
            }

            final ConversationMessage userMessage = this.conversationMessageRepository.findById(execution.getInputMessageId())
                    .orElseThrow(() -> new AgentNotFoundException("User message not found"));
            final List<ConversationMessage> lastMessages = this.conversationMessageRepository.findLastByConversationIdOrderByCreatedAtAsc(
                    conversation.getId(),
                    this.contextOptimizerProperties.getLastMessagesLimit()
            );
            final List<AgentRule> activeRules = this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                    execution.getAgentId(),
                    execution.getUserId(),
                    AgentRuleStatus.ACTIVE,
                    null
            );
            final Optional<ConversationContextSnapshot> snapshot = this.conversationContextSnapshotRepository.findByConversationId(conversation.getId());

            final UserAgentExecutionContext contextPrompt = this.conversationContextBuilder.build(
                    agent.getInstruction(),
                    activeRules,
                    snapshot.map(ConversationContextSnapshot::getSummary).orElse(""),
                    lastMessages,
                    userMessage
            );
            final String replyContent = this.agentExecutionService.execute(agent, contextPrompt);
            final ConversationMessage assistantMessage = this.conversationMessageRepository.save(this.buildAgentMessage(
                    conversation.getId(),
                    execution.getAgentId(),
                    replyContent
            ));
            log.info("[CHAT_EXECUTION] assistant message saved executionId={} assistantMessageId={}",
                    execution.getExecutionId(), assistantMessage.getId());

            final Instant completedAt = assistantMessage.getCreatedAt();
            this.conversationRepository.save(conversation.toBuilder()
                    .updatedAt(completedAt)
                    .lastMessageAt(completedAt)
                    .build());
            this.postChatWorkflowDispatcher.dispatch(new ChatCompletedContext(execution.getAgentId(), conversation.getId(), userMessage));

            execution = this.chatExecutionRepository.save(execution.toBuilder()
                    .status(ChatExecutionStatus.COMPLETED)
                    .assistantMessageId(assistantMessage.getId())
                    .completedAt(completedAt)
                    .build());
            log.info("[CHAT_EXECUTION] execution COMPLETED executionId={}", execution.getExecutionId());
        } catch (Exception exception) {
            final ChatExecutionFailure failure = this.toFailure(exception);
            this.chatExecutionRepository.save(execution.toBuilder()
                    .status(ChatExecutionStatus.FAILED)
                    .failure(failure)
                    .completedAt(Instant.now())
                    .build());
            log.warn("[CHAT_EXECUTION] execution FAILED executionId={} errorCode={}",
                    execution.getExecutionId(),
                    failure.getFailureClass(),
                    exception);
        }
    }

    private ConversationMessage buildAgentMessage(final UUID conversationId, final UUID agentId, final String message) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.AGENT)
                .authorId(agentId.toString())
                .content(message)
                .createdAt(Instant.now())
                .build();
    }

    private ChatExecutionFailure toFailure(final Exception exception) {
        if (exception instanceof AgentNotFoundException) {
            return ChatExecutionFailure.builder()
                    .failureClass(ChatExecutionFailureClass.CONVERSATION_NOT_FOUND)
                    .reason(exception.getMessage())
                    .retryable(false)
                    .build();
        }
        if (exception instanceof AgentChatNotAllowedException) {
            return ChatExecutionFailure.builder()
                    .failureClass(ChatExecutionFailureClass.INVALID_LIFECYCLE_STATE)
                    .reason(exception.getMessage())
                    .retryable(false)
                    .build();
        }
        return ChatExecutionFailure.builder()
                .failureClass(ChatExecutionFailureClass.EXECUTION_ERROR)
                .reason("Execution failed")
                .retryable(true)
                .build();
    }
}
