package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, UUID> {

    List<AgentEntity> findAllByUserIdAndTypeAndStatusIdNotOrderByUpdatedAtDesc(Long userId, AgentType type, Long statusId);

    Optional<AgentEntity> findByAgentIdAndUserIdAndTypeAndStatusIdNot(UUID agentId, Long userId, AgentType type, Long statusId);

    Optional<AgentEntity> findByAgentIdAndUserIdAndType(UUID agentId, Long userId, AgentType type);

    Optional<AgentEntity> findByAgentId(UUID agentId);

    Optional<AgentEntity> findFirstByType(AgentType type);
}
