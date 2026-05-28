package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEdgeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProjectFlowEdgeJpaRepository extends JpaRepository<AgentProjectFlowEdgeEntity, UUID> {

    List<AgentProjectFlowEdgeEntity> findByFlowFlowIdOrderByCreatedAtAsc(UUID flowId);
}
