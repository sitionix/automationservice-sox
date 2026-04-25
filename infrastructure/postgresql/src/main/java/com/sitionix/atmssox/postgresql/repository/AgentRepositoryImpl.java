package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentInfraMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentRepositoryImpl implements AgentRepository {

    private final AgentJpaRepository agentJpaRepository;

    private final AgentInfraMapper agentInfraMapper;

    @Override
    public Agent save(final Agent agent) {
        final AgentEntity entity = this.agentJpaRepository.save(this.agentInfraMapper.asAgentEntity(agent));
        return this.agentInfraMapper.asAgent(entity);
    }

    @Override
    public List<Agent> findAllVisibleByUserId(final Long userId) {
        return this.agentJpaRepository.findAllByUserIdAndStatusIdNotOrderByUpdatedAtDesc(
                        userId,
                        AgentStatus.DELETED.getId()
                ).stream()
                .map(this.agentInfraMapper::asAgent)
                .toList();
    }

    @Override
    public Optional<Agent> findVisibleByIdAndUserId(final UUID agentId, final Long userId) {
        return this.agentJpaRepository.findByAgentIdAndUserIdAndStatusIdNot(
                        agentId,
                        userId,
                        AgentStatus.DELETED.getId()
                )
                .map(this.agentInfraMapper::asAgent);
    }

    @Override
    public Optional<Agent> findByIdAndUserId(final UUID agentId, final Long userId) {
        return this.agentJpaRepository.findByAgentIdAndUserId(agentId, userId)
                .map(this.agentInfraMapper::asAgent);
    }

    @Override
    public Optional<Agent> findById(final UUID agentId) {
        return this.agentJpaRepository.findByAgentId(agentId)
                .map(this.agentInfraMapper::asAgent);
    }

    @Override
    public Optional<Agent> findSystemRuleAnalyzer() {
        return this.agentJpaRepository.findFirstByTypeIdOrderByCreatedAtAsc(AgentType.SYSTEM_RULE_ANALYZER.getId())
                .map(this.agentInfraMapper::asAgent);
    }
}
