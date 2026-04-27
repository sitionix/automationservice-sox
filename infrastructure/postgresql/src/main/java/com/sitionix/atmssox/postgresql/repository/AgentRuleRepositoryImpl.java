package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentRuleJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentRuleAuthorTypeInfraMapper;
import com.sitionix.atmssox.postgresql.mapper.AgentRuleInfraMapper;
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
    private final AgentRuleInfraMapper agentRuleInfraMapper;
    private final AgentRuleAuthorTypeInfraMapper agentRuleAuthorTypeInfraMapper;

    @Override
    public AgentRule save(final AgentRule rule) {
        return this.agentRuleInfraMapper.asAgentRule(this.agentRuleJpaRepository.save(this.agentRuleInfraMapper.asAgentRuleEntity(rule)));
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
                    this.agentRuleAuthorTypeInfraMapper.asAuthorTypeEntity(authorType)
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
                    this.agentRuleAuthorTypeInfraMapper.asAuthorTypeEntity(authorType)
            );
        } else {
            entities = this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdOrderByCreatedAtAsc(agentId, userId);
        }
        return entities.stream().map(this.agentRuleInfraMapper::asAgentRule).toList();
    }

    @Override
    public Optional<AgentRule> findByIdAndAgentIdAndUserId(final UUID ruleId, final UUID agentId, final Long userId) {
        return this.agentRuleJpaRepository.findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId)
                .map(this.agentRuleInfraMapper::asAgentRule);
    }

    @Override
    public long countByAgentIdAndStatusAndAuthorType(final UUID agentId,
                                                     final AgentRuleStatus status,
                                                     final AgentRuleAuthorType authorType) {
        return this.agentRuleJpaRepository.countByAgentAgentIdAndStatusIdAndAuthorType(
                agentId,
                status.getId(),
                this.agentRuleAuthorTypeInfraMapper.asAuthorTypeEntity(authorType)
        );
    }

    @Override
    public long countByAgentIdAndAuthorTypeAndCreatedAtBetween(final UUID agentId,
                                                               final AgentRuleAuthorType authorType,
                                                               final Instant startInclusive,
                                                               final Instant endExclusive) {
        return this.agentRuleJpaRepository.countByAgentAgentIdAndAuthorTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                agentId,
                this.agentRuleAuthorTypeInfraMapper.asAuthorTypeEntity(authorType),
                startInclusive,
                endExclusive
        );
    }

    @Override
    public Optional<Instant> findLastCreatedAtByAgentIdAndAuthorType(final UUID agentId, final AgentRuleAuthorType authorType) {
        return this.agentRuleJpaRepository.findFirstByAgentAgentIdAndAuthorTypeOrderByCreatedAtDesc(
                        agentId,
                        this.agentRuleAuthorTypeInfraMapper.asAuthorTypeEntity(authorType)
                )
                .map(AgentRuleEntity::getCreatedAt);
    }
}
