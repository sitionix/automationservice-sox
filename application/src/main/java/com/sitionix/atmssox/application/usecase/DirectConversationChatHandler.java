package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectConversationChatHandler implements ConversationChatHandler {

    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationContextSnapshotRepository conversationContextSnapshotRepository;
    private final ConversationContextBuilder conversationContextBuilder;
    private final ContextOptimizerProperties contextOptimizerProperties;
    private final AgentExecutionService agentExecutionService;
    private final PostChatWorkflowDispatcher postChatWorkflowDispatcher;

    @Override
    public ChatAgentResponse handle(final Conversation conversation,
                                    final List<ConversationParticipant> participants,
                                    final ChatAgentCommand command,
                                    final Long userId) {
        if (conversation.getType() != ConversationType.DIRECT) {
            throw new AgentValidationException("Conversation type is not supported by direct handler");
        }
        final UUID agentId = this.resolveAgentId(participants);
        final Agent agent = this.agentRepository.findVisibleByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        if (agent.getStatus() != AgentStatus.ACTIVE) {
            throw new AgentChatNotAllowedException("Only ACTIVE agent can execute chat");
        }
        if (!UserAgentExecutionContext.class.equals(agent.getType().supportedContextType())) {
            throw new AgentChatNotAllowedException("Only agent with USER chat context can execute chat");
        }

        final String message = this.normalizeMessage(command);
        final ConversationMessage userMessage = this.conversationMessageRepository.save(this.buildUserMessage(
                conversation.getId(),
                userId,
                message
        ));
        final List<ConversationMessage> lastMessages = this.conversationMessageRepository.findLastByConversationIdOrderByCreatedAtAsc(
                conversation.getId(),
                this.contextOptimizerProperties.getLastMessagesLimit()
        );
        final List<AgentRule> activeRules = this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                agentId,
                userId,
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
        final String replyContent = this.agentExecutionService.execute(
                agent,
                contextPrompt
        );

        final ConversationMessage reply = this.conversationMessageRepository.save(this.buildAgentMessage(conversation.getId(), agentId, replyContent));
        this.touchConversation(conversation, reply.getCreatedAt());
        this.triggerBackgroundWorkflows(new ChatCompletedContext(agentId, conversation.getId(), userMessage));

        return ChatAgentResponse.builder()
                .conversationId(conversation.getId())
                .reply(reply)
                .build();
    }

    private void touchConversation(final Conversation conversation, final Instant lastMessageAt) {
        this.conversationRepository.save(conversation.toBuilder()
                .updatedAt(lastMessageAt)
                .lastMessageAt(lastMessageAt)
                .build());
    }

    private void triggerBackgroundWorkflows(final ChatCompletedContext context) {
        this.postChatWorkflowDispatcher.dispatch(context);
    }

    private UUID resolveAgentId(final List<ConversationParticipant> participants) {
        final ConversationParticipant agentParticipant = participants.stream()
                .filter(participant -> participant.getParticipantType() == ConversationParticipantType.AGENT)
                .findFirst()
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        final String participantId = agentParticipant.getParticipantId();
        try {
            return UUID.fromString(participantId);
        } catch (IllegalArgumentException exception) {
            throw new AgentValidationException("Invalid AGENT participant identifier");
        }
    }

    private ConversationMessage buildUserMessage(final UUID conversationId, final Long userId, final String message) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.USER)
                .authorId(String.valueOf(userId))
                .content(message)
                .createdAt(Instant.now())
                .build();
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

    private String normalizeMessage(final ChatAgentCommand command) {
        if (command == null || command.getMessage() == null || command.getMessage().trim().isEmpty()) {
            throw new AgentValidationException("Message must not be blank");
        }
        return command.getMessage().trim();
    }

}
