package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for one conversation context snapshot.
 */
public interface ConversationContextSnapshotRepository {

    /**
     * Stores one snapshot.
     *
     * @param snapshot snapshot state to persist.
     * @return persisted snapshot.
     */
    ConversationContextSnapshot save(ConversationContextSnapshot snapshot);

    /**
     * Finds current snapshot by conversation id.
     *
     * @param conversationId conversation identifier.
     * @return snapshot when present.
     */
    Optional<ConversationContextSnapshot> findByConversationId(UUID conversationId);
}
