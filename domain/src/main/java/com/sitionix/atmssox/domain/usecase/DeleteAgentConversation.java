package com.sitionix.atmssox.domain.usecase;

import java.util.UUID;

/**
 * Soft deletes one direct agent conversation.
 */
public interface DeleteAgentConversation {

    /**
     * Marks one conversation as deleted for current user.
     *
     * @param conversationId unique conversation identifier.
     */
    void execute(UUID conversationId);
}
