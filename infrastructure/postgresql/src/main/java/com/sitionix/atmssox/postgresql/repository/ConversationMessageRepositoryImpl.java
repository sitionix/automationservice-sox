package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationMessageJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public Optional<ConversationMessage> findById(final UUID messageId) {
        return this.conversationMessageJpaRepository.findById(messageId).map(this::toDomain);
    }

    @Override
    public List<ConversationMessage> findAllByConversationIdOrderByCreatedAtAsc(final UUID conversationId) {
        return this.conversationMessageJpaRepository.findAllByConversationConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ConversationMessage> findLastByConversationIdOrderByCreatedAtAsc(final UUID conversationId, final int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return this.conversationMessageJpaRepository
                .findAllByConversationConversationIdOrderByCreatedAtDescMessageIdDesc(conversationId, PageRequest.of(0, limit))
                .reversed()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<ConversationMessage> findLastByConversationIdAndAuthorType(final UUID conversationId,
                                                                                final ConversationParticipantType authorType) {
        return this.conversationMessageJpaRepository
                .findFirstByConversationConversationIdAndAuthorTypeOrderByCreatedAtDescMessageIdDesc(conversationId, authorType)
                .map(this::toDomain);
    }

    @Override
    public List<ConversationMessage> findSliceByConversationIdOrderByCreatedAtAsc(final UUID conversationId,
                                                                                   final int offset,
                                                                                   final int limit) {
        if (limit <= 0) {
            return List.of();
        }
        final int normalizedOffset = Math.max(0, offset);
        return this.conversationMessageJpaRepository
                .findSliceByConversationIdOrderByCreatedAtAsc(conversationId, normalizedOffset, limit)
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
