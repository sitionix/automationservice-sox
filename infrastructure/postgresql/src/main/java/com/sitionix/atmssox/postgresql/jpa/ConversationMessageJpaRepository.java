package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationMessageJpaRepository extends JpaRepository<ConversationMessageEntity, UUID> {

    List<ConversationMessageEntity> findAllByConversationConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<ConversationMessageEntity> findAllByConversationConversationIdOrderByCreatedAtDescMessageIdDesc(UUID conversationId, Pageable pageable);

    Optional<ConversationMessageEntity> findFirstByConversationConversationIdAndAuthorTypeOrderByCreatedAtDescMessageIdDesc(
            UUID conversationId,
            ConversationParticipantType authorType
    );

    @Query(value = """
            SELECT *
            FROM conversation_messages
            WHERE conversation_id = :conversationId
            ORDER BY created_at ASC, message_id ASC
            OFFSET :offset
            LIMIT :limit
            """, nativeQuery = true)
    List<ConversationMessageEntity> findSliceByConversationIdOrderByCreatedAtAsc(
            @Param("conversationId") UUID conversationId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countByConversationConversationIdAndAuthorType(UUID conversationId, ConversationParticipantType authorType);

    long countByConversationConversationId(UUID conversationId);
}
