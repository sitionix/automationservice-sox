package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ConversationParticipant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence contract for conversation participants.
 */
public interface ConversationParticipantRepository {

    /**
     * Persists conversation participants.
     *
     * @param participants participants to persist.
     */
    void saveAll(List<ConversationParticipant> participants);

    /**
     * Loads all participants by conversation identifier.
     *
     * @param conversationId conversation identifier.
     * @return participants associated with conversation.
     */
    List<ConversationParticipant> findAllByConversationId(UUID conversationId);
}
