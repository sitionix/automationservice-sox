package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleAuthorTypeEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleStatusEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentRuleJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentRuleRepositoryImpl implements AgentRuleRepository {

    private final AgentRuleJpaRepository agentRuleJpaRepository;

    @Override
    public AgentRule save(final AgentRule rule) {
        return this.toDomain(this.agentRuleJpaRepository.save(this.toEntity(rule)));
    }

    @Override
    public List<AgentRule> findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(final UUID agentId,
                                                                                   final Long userId,
                                                                                   final AgentRuleStatus status,
                                                                                   final AgentRuleAuthorType authorType) {
        final List<AgentRuleEntity> entities;
        if (status != null && authorType != null) {
            entities = this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdAndStatusIdAndAuthorTypeOrderByCreatedAtAsc(
                    agentId,
                    userId,
                    status.getId(),
                    this.toAuthorTypeEntity(authorType)
            );
        } else if (status != null) {
            entities = this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(
                    agentId,
                    userId,
                    status.getId()
            );
        } else if (authorType != null) {
            entities = this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdAndAuthorTypeOrderByCreatedAtAsc(
                    agentId,
                    userId,
                    this.toAuthorTypeEntity(authorType)
            );
        } else {
            entities = this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdOrderByCreatedAtAsc(agentId, userId);
        }
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<AgentRule> findByIdAndAgentIdAndUserId(final UUID ruleId, final UUID agentId, final Long userId) {
        return this.agentRuleJpaRepository.findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId)
                .map(this::toDomain);
    }

    @Override
    public long countByAgentIdAndStatusAndAuthorType(final UUID agentId,
                                                     final AgentRuleStatus status,
                                                     final AgentRuleAuthorType authorType) {
        return this.agentRuleJpaRepository.countByAgentAgentIdAndStatusIdAndAuthorType(
                agentId,
                status.getId(),
                this.toAuthorTypeEntity(authorType)
        );
    }

    @Override
    public long countByAgentIdAndAuthorTypeAndCreatedAtBetween(final UUID agentId,
                                                               final AgentRuleAuthorType authorType,
                                                               final Instant startInclusive,
                                                               final Instant endExclusive) {
        return this.agentRuleJpaRepository.countByAgentAgentIdAndAuthorTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                agentId,
                this.toAuthorTypeEntity(authorType),
                startInclusive,
                endExclusive
        );
    }

    @Override
    public Optional<Instant> findLastCreatedAtByAgentIdAndAuthorType(final UUID agentId, final AgentRuleAuthorType authorType) {
        return this.agentRuleJpaRepository.findFirstByAgentAgentIdAndAuthorTypeOrderByCreatedAtDesc(
                        agentId,
                        this.toAuthorTypeEntity(authorType)
                )
                .map(AgentRuleEntity::getCreatedAt);
    }

    private AgentRuleEntity toEntity(final AgentRule rule) {
        final AgentEntity agent = new AgentEntity();
        agent.setAgentId(rule.getAgentId());
        final AgentRuleStatusEntity status = AgentRuleStatusEntity.builder()
                .id(rule.getStatus().getId())
                .description(rule.getStatus().name())
                .build();
        final AgentRuleAuthorTypeEntity authorType = this.toAuthorTypeEntity(rule.getAuthorType());

        return AgentRuleEntity.builder()
                .ruleId(rule.getId())
                .agent(agent)
                .title(rule.getTitle())
                .content(rule.getContent())
                .status(status)
                .authorType(authorType)
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private AgentRuleAuthorTypeEntity toAuthorTypeEntity(final AgentRuleAuthorType authorType) {
        return AgentRuleAuthorTypeEntity.builder()
                .id(authorType.getId())
                .description(authorType.name())
                .build();
    }

    private AgentRule toDomain(final AgentRuleEntity entity) {
        return AgentRule.builder()
                .id(entity.getRuleId())
                .agentId(entity.getAgent().getAgentId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .status(AgentRuleStatus.fromId(entity.getStatus().getId()))
                .authorType(AgentRuleAuthorType.fromId(entity.getAuthorType().getId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
