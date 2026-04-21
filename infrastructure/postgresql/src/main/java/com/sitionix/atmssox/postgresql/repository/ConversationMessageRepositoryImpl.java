package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationMessageJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationMessageRepositoryImpl implements ConversationMessageRepository {

    private final ConversationMessageJpaRepository conversationMessageJpaRepository;
    private final EntityManager entityManager;

    @Override
    public ConversationMessage save(final ConversationMessage message) {
        return this.toDomain(this.conversationMessageJpaRepository.save(this.toEntity(message)));
    }

    @Override
    public List<ConversationMessage> findAllByConversationIdOrderByCreatedAtAsc(final UUID conversationId) {
        return this.conversationMessageJpaRepository.findAllByConversationConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ConversationMessageEntity toEntity(final ConversationMessage message) {
        final ConversationEntity conversationRef = this.entityManager.getReference(ConversationEntity.class, message.getConversationId());
        return new ConversationMessageEntity(
                message.getId(),
                conversationRef,
                message.getAuthorType(),
                message.getAuthorId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private ConversationMessage toDomain(final ConversationMessageEntity entity) {
        return ConversationMessage.builder()
                .id(entity.getMessageId())
                .conversationId(entity.getConversation().getConversationId())
                .authorType(entity.getAuthorType())
                .authorId(entity.getAuthorId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
