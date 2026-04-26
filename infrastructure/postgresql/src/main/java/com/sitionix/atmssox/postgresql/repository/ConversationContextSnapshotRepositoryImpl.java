package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationContextSnapshotEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationContextSnapshotJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationContextSnapshotRepositoryImpl implements ConversationContextSnapshotRepository {

    private final ConversationContextSnapshotJpaRepository conversationContextSnapshotJpaRepository;

    @Override
    public ConversationContextSnapshot save(final ConversationContextSnapshot snapshot) {
        return this.toDomain(this.conversationContextSnapshotJpaRepository.save(this.toEntity(snapshot)));
    }

    @Override
    public Optional<ConversationContextSnapshot> findByConversationId(final UUID conversationId) {
        return this.conversationContextSnapshotJpaRepository.findByConversationConversationId(conversationId)
                .map(this::toDomain);
    }

    private ConversationContextSnapshotEntity toEntity(final ConversationContextSnapshot snapshot) {
        return ConversationContextSnapshotEntity.builder()
                .id(snapshot.getId())
                .conversation(this.getConversationRef(snapshot.getConversationId()))
                .summary(snapshot.getSummary())
                .messageCountUntil(snapshot.getMessageCountUntil())
                .lastMessageIdUntil(snapshot.getLastMessageIdUntil())
                .createdAt(snapshot.getCreatedAt())
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }

    private ConversationContextSnapshot toDomain(final ConversationContextSnapshotEntity entity) {
        return ConversationContextSnapshot.builder()
                .id(entity.getId())
                .conversationId(entity.getConversation().getConversationId())
                .summary(entity.getSummary())
                .messageCountUntil(entity.getMessageCountUntil())
                .lastMessageIdUntil(entity.getLastMessageIdUntil())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ConversationEntity getConversationRef(final UUID conversationId) {
        return ConversationEntity.builder()
                .conversationId(conversationId)
                .build();
    }
}
