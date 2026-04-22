package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ConversationDetails;
import java.util.UUID;

/**
 * Loads one direct conversation with full message history.
 */
public interface GetAgentConversation {

    /**
     * Returns one active direct conversation.
     *
     * @param conversationId conversation identifier.
     * @return conversation metadata with ordered messages.
     */
    ConversationDetails execute(UUID conversationId);
}
