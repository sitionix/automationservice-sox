package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.Agent;
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
    public List<Agent> findAll() {
        return this.agentJpaRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this.agentInfraMapper::asAgent)
                .toList();
    }

    @Override
    public Optional<Agent> findById(final UUID agentId) {
        return this.agentJpaRepository.findById(agentId)
                .map(this.agentInfraMapper::asAgent);
    }
}
