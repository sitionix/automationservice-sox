package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ConversationParticipant;
import java.util.List;

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
}
