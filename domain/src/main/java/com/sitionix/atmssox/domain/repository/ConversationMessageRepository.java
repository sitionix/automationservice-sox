package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.List;
import java.util.UUID;

/**
 * Persistence contract for conversation messages.
 */
public interface ConversationMessageRepository {

    /**
     * Persists one conversation message.
     *
     * @param message message to persist.
     * @return persisted message.
     */
    ConversationMessage save(ConversationMessage message);

    /**
     * Loads conversation messages ordered by creation time ascending.
     *
     * @param conversationId conversation identifier.
     * @return ordered message history.
     */
    List<ConversationMessage> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
