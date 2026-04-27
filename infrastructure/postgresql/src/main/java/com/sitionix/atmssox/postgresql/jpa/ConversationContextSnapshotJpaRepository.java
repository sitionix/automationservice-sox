package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.conversation.ConversationContextSnapshotEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationContextSnapshotJpaRepository extends JpaRepository<ConversationContextSnapshotEntity, UUID> {

    Optional<ConversationContextSnapshotEntity> findByConversationConversationId(UUID conversationId);
}
