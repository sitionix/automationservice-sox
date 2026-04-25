package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleStatusEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentRuleJpaRepository;
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
                    authorType
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
                    authorType
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

    private AgentRuleEntity toEntity(final AgentRule rule) {
        final AgentEntity agent = new AgentEntity();
        agent.setAgentId(rule.getAgentId());
        final AgentRuleStatusEntity status = AgentRuleStatusEntity.builder()
                .id(rule.getStatus().getId())
                .description(rule.getStatus().name())
                .build();

        return AgentRuleEntity.builder()
                .ruleId(rule.getId())
                .agent(agent)
                .title(rule.getTitle())
                .content(rule.getContent())
                .status(status)
                .authorType(rule.getAuthorType())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private AgentRule toDomain(final AgentRuleEntity entity) {
        return AgentRule.builder()
                .id(entity.getRuleId())
                .agentId(entity.getAgent().getAgentId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .status(AgentRuleStatus.fromId(entity.getStatus().getId()))
                .authorType(entity.getAuthorType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
