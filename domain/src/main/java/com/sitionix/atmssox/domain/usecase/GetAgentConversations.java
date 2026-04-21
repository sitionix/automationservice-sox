package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Conversation;
import java.util.List;
import java.util.UUID;

/**
 * Loads direct conversations for one agent context.
 */
public interface GetAgentConversations {

    /**
     * Returns active direct conversations for one agent.
     *
     * @param agentId agent identifier.
     * @return conversations sorted by recency.
     */
    List<Conversation> execute(UUID agentId);
}
