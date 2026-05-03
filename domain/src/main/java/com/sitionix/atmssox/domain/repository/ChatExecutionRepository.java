package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatExecutionRepository {

    ChatExecution save(ChatExecution execution);

    Optional<ChatExecution> findByExecutionId(UUID executionId);

    Optional<ChatExecution> findByAgentIdAndExecutionId(UUID agentId, UUID executionId);

    Optional<ChatExecution> findByConversationIdAndIdempotencyKey(UUID conversationId, String idempotencyKey);

    Optional<ChatExecution> findByUserIdAndAgentIdAndIdempotencyKey(Long userId, UUID agentId, String idempotencyKey);

    Optional<ChatExecution> findByExecutionIdAndStatus(UUID executionId, ChatExecutionStatus status);

    List<ChatExecution> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
