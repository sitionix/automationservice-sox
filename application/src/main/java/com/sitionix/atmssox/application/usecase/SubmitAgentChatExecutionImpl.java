package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentAccessDeniedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.SubmitAgentChatExecution;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitAgentChatExecutionImpl implements SubmitAgentChatExecution {

    private static final int TITLE_MAX_LENGTH = 80;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ChatExecutionRepository chatExecutionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ChatExecution execute(final UUID agentId, final ChatAgentCommand command, final String idempotencyKey) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final String message = this.normalizeMessage(command);

        final Conversation conversation = this.resolveConversation(userId, agentId, command, message);
        final String normalizedIdempotencyKey = StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null;

        if (normalizedIdempotencyKey != null) {
            final ChatExecution existingExecution = this.chatExecutionRepository
                    .findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, normalizedIdempotencyKey)
                    .orElse(null);
            if (existingExecution != null && existingExecution.getConversationId().equals(conversation.getId())) {
                return existingExecution.toBuilder()
                        .idempotencyReplayed(true)
                        .build();
            }
            if (existingExecution != null) {
                throw new AgentValidationException("Idempotency key conflict");
            }
        }

        final Instant now = Instant.now();
        final ConversationMessage userMessage = this.conversationMessageRepository.save(ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversation.getId())
                .authorType(ConversationParticipantType.USER)
                .authorId(String.valueOf(userId))
                .content(message)
                .createdAt(now)
                .build());
        this.conversationRepository.save(conversation.toBuilder()
                .updatedAt(now)
                .lastMessageAt(now)
                .build());
        final ChatExecution execution = this.chatExecutionRepository.save(ChatExecution.builder()
                .executionId(UUID.randomUUID())
                .agentId(agentId)
                .conversationId(conversation.getId())
                .userId(userId)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage(message)
                .idempotencyKey(normalizedIdempotencyKey)
                .idempotencyReplayed(false)
                .inputMessageId(userMessage.getId())
                .createdAt(now)
                .build());

        log.info("[CHAT_EXECUTION] submitted executionId={} conversationId={} inputMessageId={}",
                execution.getExecutionId(), execution.getConversationId(), execution.getInputMessageId());
        log.info("[CHAT_EXECUTION] async dispatch requested executionId={}", execution.getExecutionId());
        this.applicationEventPublisher.publishEvent(new ChatExecutionSubmittedEvent(execution.getExecutionId()));
        return execution;
    }

    private Conversation resolveConversation(final Long userId, final UUID agentId, final ChatAgentCommand command, final String message) {
        if (command.getConversationId() == null) {
            final Conversation conversation = this.createConversation(userId, message);
            this.conversationParticipantRepository.saveAll(this.createParticipants(conversation.getId(), agentId, userId, conversation.getCreatedAt()));
            return conversation;
        }

        final Conversation userConversation = this.conversationRepository
                .findActiveByIdAndUserIdAndAgentId(command.getConversationId(), userId, agentId)
                .orElse(null);
        if (userConversation != null) {
            return userConversation;
        }

        final Conversation anyConversation = this.conversationRepository
                .findActiveByIdAndAgentId(command.getConversationId(), agentId)
                .orElse(null);
        if (anyConversation != null) {
            throw new AgentAccessDeniedException("Conversation is not accessible");
        }

        throw new AgentNotFoundException("Conversation not found");
    }

    private Conversation createConversation(final Long userId, final String firstMessage) {
        final Instant now = Instant.now();
        return this.conversationRepository.save(Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(this.buildTitle(firstMessage))
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastMessageAt(now)
                .build());
    }

    private List<ConversationParticipant> createParticipants(final UUID conversationId,
                                                             final UUID agentId,
                                                             final Long userId,
                                                             final Instant joinedAt) {
        return List.of(
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.USER)
                        .participantId(String.valueOf(userId))
                        .joinedAt(joinedAt)
                        .build(),
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId(agentId.toString())
                        .joinedAt(joinedAt)
                        .build()
        );
    }

    private String buildTitle(final String firstMessage) {
        if (firstMessage.length() <= TITLE_MAX_LENGTH) {
            return firstMessage;
        }
        return firstMessage.substring(0, TITLE_MAX_LENGTH);
    }

    private String normalizeMessage(final ChatAgentCommand command) {
        if (command == null || command.getMessage() == null || command.getMessage().trim().isEmpty()) {
            throw new AgentValidationException("Message must not be blank");
        }
        return command.getMessage().trim();
    }

}
