package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.RuleSuggestionAnalysisRun;
import com.sitionix.atmssox.domain.repository.RuleSuggestionAnalysisRunRepository;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.analysis.RuleSuggestionAnalysisRunEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.jpa.RuleSuggestionAnalysisRunJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RuleSuggestionAnalysisRunRepositoryImpl implements RuleSuggestionAnalysisRunRepository {

    private final RuleSuggestionAnalysisRunJpaRepository ruleSuggestionAnalysisRunJpaRepository;

    @Override
    public RuleSuggestionAnalysisRun save(final RuleSuggestionAnalysisRun run) {
        final RuleSuggestionAnalysisRunEntity persisted = this.ruleSuggestionAnalysisRunJpaRepository.save(this.toEntity(run));
        return this.toDomain(persisted);
    }

    @Override
    public Optional<RuleSuggestionAnalysisRun> findLatestByAgentIdAndConversationId(final UUID agentId, final UUID conversationId) {
        return this.ruleSuggestionAnalysisRunJpaRepository
                .findFirstByAgentAgentIdAndConversationConversationIdOrderByCreatedAtDesc(agentId, conversationId)
                .map(this::toDomain);
    }

    @Override
    public long countByAgentIdAndCreatedAtBetween(final UUID agentId, final Instant fromInclusive, final Instant toExclusive) {
        return this.ruleSuggestionAnalysisRunJpaRepository
                .countByAgentAgentIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(agentId, fromInclusive, toExclusive);
    }

    private RuleSuggestionAnalysisRunEntity toEntity(final RuleSuggestionAnalysisRun run) {
        return RuleSuggestionAnalysisRunEntity.builder()
                .analysisId(run.getId())
                .agent(this.getAgentRef(run.getAgentId()))
                .conversation(this.getConversationRef(run.getConversationId()))
                .userMessageCount(run.getUserMessageCount())
                .createdAt(run.getCreatedAt())
                .build();
    }

    private RuleSuggestionAnalysisRun toDomain(final RuleSuggestionAnalysisRunEntity entity) {
        return RuleSuggestionAnalysisRun.builder()
                .id(entity.getAnalysisId())
                .agentId(entity.getAgent().getAgentId())
                .conversationId(entity.getConversation().getConversationId())
                .userMessageCount(entity.getUserMessageCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private AgentEntity getAgentRef(final UUID agentId) {
        final AgentEntity agentEntity = new AgentEntity();
        agentEntity.setAgentId(agentId);
        return agentEntity;
    }

    private ConversationEntity getConversationRef(final UUID conversationId) {
        return ConversationEntity.builder()
                .conversationId(conversationId)
                .build();
    }
}
