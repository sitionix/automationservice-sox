package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import java.util.List;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.postgresql.jpa.ChatExecutionJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.ChatExecutionInfraMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatExecutionRepositoryImpl implements ChatExecutionRepository {

    private final ChatExecutionJpaRepository chatExecutionJpaRepository;
    private final ChatExecutionInfraMapper chatExecutionInfraMapper;

    @Override
    public ChatExecution save(final ChatExecution execution) {
        return this.chatExecutionInfraMapper.asChatExecution(
                this.chatExecutionJpaRepository.save(this.chatExecutionInfraMapper.asChatExecutionEntity(execution))
        );
    }

    @Override
    public Optional<ChatExecution> findByExecutionId(final UUID executionId) {
        return this.chatExecutionJpaRepository.findById(executionId).map(this.chatExecutionInfraMapper::asChatExecution);
    }

    @Override
    public Optional<ChatExecution> findByAgentIdAndExecutionId(final UUID agentId, final UUID executionId) {
        return this.chatExecutionJpaRepository.findByAgentIdAndExecutionId(agentId, executionId).map(this.chatExecutionInfraMapper::asChatExecution);
    }

    @Override
    public Optional<ChatExecution> findByConversationIdAndIdempotencyKey(final UUID conversationId, final String idempotencyKey) {
        return this.chatExecutionJpaRepository.findByConversationIdAndIdempotencyKey(conversationId, idempotencyKey)
                .map(this.chatExecutionInfraMapper::asChatExecution);
    }

    @Override
    public Optional<ChatExecution> findByUserIdAndAgentIdAndIdempotencyKey(final Long userId, final UUID agentId, final String idempotencyKey) {
        return this.chatExecutionJpaRepository.findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, idempotencyKey)
                .map(this.chatExecutionInfraMapper::asChatExecution);
    }

    @Override
    public Optional<ChatExecution> findByExecutionIdAndStatus(final UUID executionId, final ChatExecutionStatus status) {
        return this.chatExecutionJpaRepository.findByExecutionIdAndStatusId(executionId, status.getId())
                .map(this.chatExecutionInfraMapper::asChatExecution);
    }

    @Override
    public List<ChatExecution> findAllByConversationIdOrderByCreatedAtAsc(final UUID conversationId) {
        return this.chatExecutionJpaRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this.chatExecutionInfraMapper::asChatExecution)
                .toList();
    }
}
