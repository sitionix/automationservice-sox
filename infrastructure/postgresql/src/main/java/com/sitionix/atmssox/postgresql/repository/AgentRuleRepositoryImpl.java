package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentRule;
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
    public List<AgentRule> findAllByAgentIdAndUserIdAndStatusOrderByCreatedAtAsc(final UUID agentId,
                                                                                  final Long userId,
                                                                                  final AgentRuleStatus status) {
        return this.agentRuleJpaRepository
                .findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(agentId, userId, status.getId())
                .stream()
                .map(this::toDomain)
                .toList();
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
                .text(rule.getText())
                .status(status)
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private AgentRule toDomain(final AgentRuleEntity entity) {
        return AgentRule.builder()
                .id(entity.getRuleId())
                .agentId(entity.getAgent().getAgentId())
                .text(entity.getText())
                .status(AgentRuleStatus.fromId(entity.getStatus().getId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
