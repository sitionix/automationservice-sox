package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationParticipantEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationParticipantJpaRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationParticipantRepositoryImpl implements ConversationParticipantRepository {

    private final ConversationParticipantJpaRepository conversationParticipantJpaRepository;
    private final EntityManager entityManager;

    @Override
    public void saveAll(final List<ConversationParticipant> participants) {
        this.conversationParticipantJpaRepository.saveAll(participants.stream()
                .map(this::toEntity)
                .toList());
    }

    @Override
    public List<ConversationParticipant> findAllByConversationId(final UUID conversationId) {
        return this.conversationParticipantJpaRepository.findAllByConversationConversationId(conversationId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ConversationParticipantEntity toEntity(final ConversationParticipant participant) {
        final ConversationEntity conversationRef = this.entityManager.getReference(ConversationEntity.class, participant.getConversationId());
        return new ConversationParticipantEntity(
                participant.getId(),
                conversationRef,
                participant.getParticipantType(),
                participant.getParticipantId(),
                participant.getJoinedAt()
        );
    }

    private ConversationParticipant toDomain(final ConversationParticipantEntity entity) {
        return ConversationParticipant.builder()
                .id(entity.getParticipantId())
                .conversationId(entity.getConversation().getConversationId())
                .participantType(entity.getParticipantType())
                .participantId(entity.getParticipantRef())
                .joinedAt(entity.getJoinedAt())
                .build();
    }
}
