package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProjectFlowJpaRepository extends JpaRepository<AgentProjectFlowEntity, UUID> {

    Optional<AgentProjectFlowEntity> findByProjectProjectId(UUID projectId);
}
