package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatExecutionJpaRepository extends JpaRepository<ChatExecutionEntity, UUID> {

    Optional<ChatExecutionEntity> findByAgentIdAndExecutionId(UUID agentId, UUID executionId);

    Optional<ChatExecutionEntity> findByConversationIdAndIdempotencyKey(UUID conversationId, String idempotencyKey);

    Optional<ChatExecutionEntity> findByUserIdAndAgentIdAndIdempotencyKey(Long userId, UUID agentId, String idempotencyKey);

    Optional<ChatExecutionEntity> findByExecutionIdAndStatusId(UUID executionId, Long statusId);

    List<ChatExecutionEntity> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
