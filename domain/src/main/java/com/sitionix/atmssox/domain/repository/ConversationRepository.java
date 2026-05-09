package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for direct agent conversations.
 */
public interface ConversationRepository {

    /**
     * Persists conversation metadata.
     *
     * @param conversation conversation metadata.
     * @return persisted conversation metadata.
     */
    Conversation save(Conversation conversation);

    /**
     * Loads active conversation owned by current user.
     *
     * @param conversationId conversation identifier.
     * @param userId current user identifier.
     * @return active conversation when present.
     */
    Optional<Conversation> findActiveByIdAndUserId(UUID conversationId, Long userId);

    /**
     * Loads conversation by identifier for current user regardless of lifecycle status.
     *
     * @param conversationId conversation identifier.
     * @param userId current user identifier.
     * @return conversation when present.
     */
    Optional<Conversation> findByIdAndUserId(UUID conversationId, Long userId);

    /**
     * Loads active conversation owned by current user and scoped to one agent.
     *
     * @param conversationId conversation identifier.
     * @param userId current user identifier.
     * @param agentId agent identifier.
     * @return active conversation when present.
     */
    Optional<Conversation> findActiveByIdAndUserIdAndAgentId(UUID conversationId, Long userId, UUID agentId);

    Optional<Conversation> findActiveByIdAndAgentId(UUID conversationId, UUID agentId);

    Optional<Conversation> findActiveByIdAndUserIdAndProjectId(UUID conversationId, Long userId, UUID projectId);

    /**
     * Loads all active conversations for one user-agent context.
     *
     * @param userId current user identifier.
     * @param agentId agent identifier.
     * @return active conversations sorted by recency.
     */
    List<Conversation> findAllActiveByUserIdAndAgentId(Long userId, UUID agentId);

    List<Conversation> findAllActiveByUserIdAndProjectId(Long userId, UUID projectId);
}
