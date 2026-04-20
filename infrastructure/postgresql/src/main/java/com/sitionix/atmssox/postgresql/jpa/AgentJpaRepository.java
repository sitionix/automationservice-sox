package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, UUID> {

    List<AgentEntity> findAllByUserIdAndStatusIdNotOrderByUpdatedAtDesc(Long userId, Long statusId);

    java.util.Optional<AgentEntity> findByAgentIdAndUserIdAndStatusIdNot(UUID agentId, Long userId, Long statusId);

    java.util.Optional<AgentEntity> findByAgentIdAndUserId(UUID agentId, Long userId);
}
