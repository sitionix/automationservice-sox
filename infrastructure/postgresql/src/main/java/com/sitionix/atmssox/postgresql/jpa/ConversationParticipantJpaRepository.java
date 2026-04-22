package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.conversation.ConversationParticipantEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantJpaRepository extends JpaRepository<ConversationParticipantEntity, UUID> {

    List<ConversationParticipantEntity> findAllByConversationConversationId(UUID conversationId);
}
