package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ConversationDetails;
import java.util.UUID;

/**
 * Loads one direct conversation with full message history.
 */
public interface GetAgentConversation {

    /**
     * Returns one active direct conversation for one agent.
     *
     * @param agentId agent identifier.
     * @param conversationId conversation identifier.
     * @return conversation metadata with ordered messages.
     */
    ConversationDetails execute(UUID agentId, UUID conversationId);
}
