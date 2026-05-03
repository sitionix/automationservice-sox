package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import java.util.List;
import java.util.Optional;
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

    Optional<ConversationMessage> findById(UUID messageId);

    /**
     * Loads conversation messages ordered by creation time ascending.
     *
     * @param conversationId conversation identifier.
     * @return ordered message history.
     */
    List<ConversationMessage> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    /**
     * Loads latest N conversation messages ordered by creation time ascending.
     *
     * @param conversationId conversation identifier.
     * @param limit max messages to load.
     * @return ordered latest messages.
     */
    List<ConversationMessage> findLastByConversationIdOrderByCreatedAtAsc(UUID conversationId, int limit);

    /**
     * Loads one latest message by author type.
     *
     * @param conversationId conversation identifier.
     * @param authorType message author type.
     * @return latest matching message when present.
     */
    Optional<ConversationMessage> findLastByConversationIdAndAuthorType(UUID conversationId, ConversationParticipantType authorType);

    /**
     * Loads one ordered message slice.
     *
     * @param conversationId conversation identifier.
     * @param offset zero-based start index in ordered history.
     * @param limit max messages to load.
     * @return ordered message slice.
     */
    List<ConversationMessage> findSliceByConversationIdOrderByCreatedAtAsc(UUID conversationId, int offset, int limit);

    /**
     * Counts messages for one conversation and author type.
     *
     * @param conversationId conversation identifier.
     * @param authorType message author type.
     * @return count of matching messages.
     */
    long countByConversationIdAndAuthorType(UUID conversationId, ConversationParticipantType authorType);

    /**
     * Counts all messages for one conversation.
     *
     * @param conversationId conversation identifier.
     * @return total message count.
     */
    long countByConversationId(UUID conversationId);
}
