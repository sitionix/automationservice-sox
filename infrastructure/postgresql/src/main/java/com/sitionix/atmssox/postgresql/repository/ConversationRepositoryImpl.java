package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepository {

    private final ConversationJpaRepository conversationJpaRepository;

    @Override
    public Conversation save(final Conversation conversation) {
        return this.toDomain(this.conversationJpaRepository.save(this.toEntity(conversation)));
    }

    @Override
    public Optional<Conversation> findActiveByIdAndUserId(final UUID conversationId, final Long userId) {
        return this.conversationJpaRepository
                .findActiveByIdAndUserId(conversationId, ConversationStatus.ACTIVE, userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByIdAndUserId(final UUID conversationId, final Long userId) {
        return this.conversationJpaRepository
                .findByIdAndUserId(conversationId, userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findActiveByIdAndUserIdAndAgentId(final UUID conversationId, final Long userId, final UUID agentId) {
        return this.conversationJpaRepository
                .findActiveByIdAndUserIdAndAgent(
                        conversationId,
                        ConversationStatus.ACTIVE,
                        userId,
                        ConversationParticipantType.AGENT,
                        agentId.toString()
                )
                .map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findActiveByIdAndAgentId(final UUID conversationId, final UUID agentId) {
        return this.conversationJpaRepository
                .findActiveByIdAndAgent(
                        conversationId,
                        ConversationStatus.ACTIVE,
                        ConversationParticipantType.AGENT,
                        agentId.toString()
                )
                .map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findActiveByIdAndUserIdAndProjectId(final UUID conversationId, final Long userId, final UUID projectId) {
        return this.conversationJpaRepository
                .findActiveByIdAndUserIdAndProjectId(conversationId, ConversationStatus.ACTIVE, userId, projectId)
                .map(this::toDomain);
    }

    @Override
    public List<Conversation> findAllActiveByUserIdAndAgentId(final Long userId, final UUID agentId) {
        return this.conversationJpaRepository
                .findAllActiveByUserIdAndAgent(
                        ConversationStatus.ACTIVE,
                        userId,
                        ConversationParticipantType.AGENT,
                        agentId.toString()
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Conversation> findAllActiveByUserIdAndProjectId(final Long userId, final UUID projectId) {
        return this.conversationJpaRepository
                .findAllActiveByUserIdAndProjectId(ConversationStatus.ACTIVE, userId, projectId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ConversationEntity toEntity(final Conversation conversation) {
        return ConversationEntity.builder()
                .conversationId(conversation.getId())
                .userId(conversation.getUserId())
                .projectId(conversation.getProjectId())
                .title(conversation.getTitle())
                .type(conversation.getType())
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessageAt(conversation.getLastMessageAt())
                .build();
    }

    private Conversation toDomain(final ConversationEntity entity) {
        return Conversation.builder()
                .id(entity.getConversationId())
                .userId(entity.getUserId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .type(entity.getType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastMessageAt(entity.getLastMessageAt())
                .build();
    }
}
