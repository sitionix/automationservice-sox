package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.jpa.ChatExecutionJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatExecutionRepositoryImpl implements ChatExecutionRepository {

    private final ChatExecutionJpaRepository chatExecutionJpaRepository;

    @Override
    public ChatExecution save(final ChatExecution execution) {
        return this.toDomain(this.chatExecutionJpaRepository.save(this.toEntity(execution)));
    }

    @Override
    public Optional<ChatExecution> findByExecutionId(final UUID executionId) {
        return this.chatExecutionJpaRepository.findById(executionId).map(this::toDomain);
    }

    @Override
    public Optional<ChatExecution> findByAgentIdAndExecutionId(final UUID agentId, final UUID executionId) {
        return this.chatExecutionJpaRepository.findByAgentIdAndExecutionId(agentId, executionId).map(this::toDomain);
    }

    @Override
    public Optional<ChatExecution> findByConversationIdAndIdempotencyKey(final UUID conversationId, final String idempotencyKey) {
        return this.chatExecutionJpaRepository.findByConversationIdAndIdempotencyKey(conversationId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<ChatExecution> findByUserIdAndAgentIdAndIdempotencyKey(final Long userId, final UUID agentId, final String idempotencyKey) {
        return this.chatExecutionJpaRepository.findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<ChatExecution> findByExecutionIdAndStatus(final UUID executionId, final ChatExecutionStatus status) {
        return this.chatExecutionJpaRepository.findByExecutionIdAndStatus(executionId, status).map(this::toDomain);
    }

    private ChatExecutionEntity toEntity(final ChatExecution src) {
        return ChatExecutionEntity.builder()
                .executionId(src.getExecutionId())
                .agentId(src.getAgentId())
                .conversationId(src.getConversationId())
                .userId(src.getUserId())
                .status(src.getStatus())
                .requestMessage(src.getRequestMessage())
                .idempotencyKey(src.getIdempotencyKey())
                .assistantMessageId(src.getAssistantMessageId())
                .failureClass(src.getFailure() == null ? null : src.getFailure().getFailureClass())
                .failureReason(src.getFailure() == null ? null : src.getFailure().getReason())
                .failureRetryable(src.getFailure() == null ? null : src.getFailure().isRetryable())
                .createdAt(src.getCreatedAt())
                .startedAt(src.getStartedAt())
                .completedAt(src.getCompletedAt())
                .build();
    }

    private ChatExecution toDomain(final ChatExecutionEntity src) {
        return ChatExecution.builder()
                .executionId(src.getExecutionId())
                .agentId(src.getAgentId())
                .conversationId(src.getConversationId())
                .userId(src.getUserId())
                .status(src.getStatus())
                .requestMessage(src.getRequestMessage())
                .idempotencyKey(src.getIdempotencyKey())
                .idempotencyReplayed(false)
                .assistantMessageId(src.getAssistantMessageId())
                .assistantMessage(null)
                .failure(src.getFailureClass() == null ? null : ChatExecutionFailure.builder()
                        .failureClass(src.getFailureClass())
                        .reason(src.getFailureReason())
                        .retryable(Boolean.TRUE.equals(src.getFailureRetryable()))
                        .build())
                .createdAt(src.getCreatedAt())
                .startedAt(src.getStartedAt())
                .completedAt(src.getCompletedAt())
                .build();
    }
}
