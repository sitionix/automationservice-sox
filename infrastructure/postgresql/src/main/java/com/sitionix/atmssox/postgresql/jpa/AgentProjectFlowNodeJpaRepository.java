package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowNodeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProjectFlowNodeJpaRepository extends JpaRepository<AgentProjectFlowNodeEntity, UUID> {

    List<AgentProjectFlowNodeEntity> findByFlowFlowIdOrderByCreatedAtAsc(UUID flowId);
}
