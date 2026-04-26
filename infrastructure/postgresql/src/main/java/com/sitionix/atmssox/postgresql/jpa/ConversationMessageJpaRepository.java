package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageJpaRepository extends JpaRepository<ConversationMessageEntity, UUID> {

    List<ConversationMessageEntity> findAllByConversationConversationIdOrderByCreatedAtAsc(UUID conversationId);

    long countByConversationConversationIdAndAuthorType(UUID conversationId, ConversationParticipantType authorType);

    long countByConversationConversationId(UUID conversationId);
}
