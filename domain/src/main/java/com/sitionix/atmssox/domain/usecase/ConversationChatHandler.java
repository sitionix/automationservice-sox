package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import java.util.List;

/**
 * Handles message processing strategy for a concrete conversation context.
 */
public interface ConversationChatHandler {

    /**
     * Processes one user message inside resolved conversation context.
     *
     * @param conversation resolved conversation metadata.
     * @param participants resolved conversation participants.
     * @param command normalized chat command.
     * @param userId current user identifier.
     * @return chat response payload with persisted reply.
     */
    ChatAgentResponse handle(Conversation conversation,
                             List<ConversationParticipant> participants,
                             ChatAgentCommand command,
                             Long userId);
}
