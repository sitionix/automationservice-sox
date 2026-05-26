package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.SubmitConversationExecution;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitConversationExecutionImpl implements SubmitConversationExecution {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ChatExecutionRepository chatExecutionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ConversationExecutionProperties conversationExecutionProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ChatExecution execute(final UUID conversationId, final String message) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final String normalizedMessage = this.normalizeMessage(message);
        final Conversation conversation = this.conversationRepository.findActiveByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));
        if (conversation.getProjectId() == null) {
            throw new AgentNotFoundException("Conversation not found");
        }
        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new AgentValidationException("Conversation is not active");
        }

        final List<ConversationParticipant> participants = this.conversationParticipantRepository.findAllByConversationId(conversationId);
        final List<ConversationParticipant> agentParticipants = participants.stream()
                .filter(participant -> participant.getParticipantType() == ConversationParticipantType.AGENT)
                .toList();
        if (agentParticipants.isEmpty()) {
            throw new AgentValidationException("Conversation must contain at least one agent participant");
        }

        final Instant now = Instant.now();
        final ConversationMessage userMessage = this.conversationMessageRepository.save(ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.USER)
                .authorId(String.valueOf(userId))
                .content(normalizedMessage)
                .createdAt(now)
                .build());
        this.conversationRepository.save(conversation.toBuilder()
                .updatedAt(now)
                .lastMessageAt(now)
                .build());
        log.info("[CONVERSATION_EXECUTION] user message persisted conversationId={} inputMessageId={}", conversationId, userMessage.getId());

        if (!this.conversationExecutionProperties.isRuntimeDispatchEnabled()) {
            return ChatExecution.builder()
                    .conversationId(conversationId)
                    .userId(userId)
                    .requestMessage(normalizedMessage)
                    .inputMessageId(userMessage.getId())
                    .createdAt(now)
                    .idempotencyReplayed(false)
                    .build();
        }

        final ChatExecution execution = this.chatExecutionRepository.save(ChatExecution.builder()
                .executionId(UUID.randomUUID())
                .agentId(UUID.fromString(agentParticipants.get(0).getParticipantId()))
                .conversationId(conversationId)
                .userId(userId)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage(normalizedMessage)
                .inputMessageId(userMessage.getId())
                .createdAt(now)
                .build()).toBuilder().idempotencyReplayed(false).build();
        this.applicationEventPublisher.publishEvent(new ChatExecutionSubmittedEvent(execution.getExecutionId()));
        return execution;
    }

    private String normalizeMessage(final String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new AgentValidationException("Message must not be blank");
        }
        return message.trim();
    }
}
