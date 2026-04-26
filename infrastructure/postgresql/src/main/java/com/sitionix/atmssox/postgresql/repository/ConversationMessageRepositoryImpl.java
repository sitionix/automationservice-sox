package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationMessageJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationMessageRepositoryImpl implements ConversationMessageRepository {

    private final ConversationMessageJpaRepository conversationMessageJpaRepository;

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

    @Override
    public long countByConversationIdAndAuthorType(final UUID conversationId, final ConversationParticipantType authorType) {
        return this.conversationMessageJpaRepository.countByConversationConversationIdAndAuthorType(conversationId, authorType);
    }

    @Override
    public long countByConversationId(final UUID conversationId) {
        return this.conversationMessageJpaRepository.countByConversationConversationId(conversationId);
    }

    private ConversationMessageEntity toEntity(final ConversationMessage message) {
        return ConversationMessageEntity.builder()
                .messageId(message.getId())
                .conversation(this.getConversationRef(message.getConversationId()))
                .authorType(message.getAuthorType())
                .authorId(message.getAuthorId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
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

    private ConversationEntity getConversationRef(final UUID conversationId) {
        return ConversationEntity.builder()
                .conversationId(conversationId)
                .build();
    }
}
