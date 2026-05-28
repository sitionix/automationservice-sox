package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.model.AgentProjectFlowEdge;
import com.sitionix.atmssox.domain.model.AgentProjectFlowNode;
import com.sitionix.atmssox.domain.repository.AgentProjectFlowRepository;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEdgeEntity;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowNodeEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowEdgeJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowNodeJpaRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentProjectFlowRepositoryImpl implements AgentProjectFlowRepository {

    private final AgentProjectFlowJpaRepository agentProjectFlowJpaRepository;
    private final AgentProjectFlowNodeJpaRepository agentProjectFlowNodeJpaRepository;
    private final AgentProjectFlowEdgeJpaRepository agentProjectFlowEdgeJpaRepository;
    @Override
    public Optional<AgentProjectFlow> findByProjectId(final UUID projectId) {
        return this.agentProjectFlowJpaRepository.findByProjectProjectId(projectId)
                .map(flowEntity -> {
                    final List<AgentProjectFlowNode> nodes = this.agentProjectFlowNodeJpaRepository
                            .findByFlowFlowIdOrderByCreatedAtAsc(flowEntity.getFlowId())
                            .stream()
                            .map(this::asNode)
                            .toList();
                    final List<AgentProjectFlowEdge> edges = this.agentProjectFlowEdgeJpaRepository
                            .findByFlowFlowIdOrderByCreatedAtAsc(flowEntity.getFlowId())
                            .stream()
                            .map(this::asEdge)
                            .toList();
                    return AgentProjectFlow.builder()
                            .flowId(flowEntity.getFlowId())
                            .nodes(nodes)
                            .edges(edges)
                            .build();
                });
    }

    private AgentProjectFlowNode asNode(final AgentProjectFlowNodeEntity entity) {
        return AgentProjectFlowNode.builder()
                .id(entity.getNodeId())
                .nodeType(entity.getNodeType())
                .referenceId(entity.getReferenceId())
                .positionX(entity.getPositionX())
                .positionY(entity.getPositionY())
                .designStatus(entity.getDesignStatus())
                .config(entity.getConfig() == null ? Collections.emptyMap() : entity.getConfig())
                .build();
    }

    private AgentProjectFlowEdge asEdge(final AgentProjectFlowEdgeEntity entity) {
        return AgentProjectFlowEdge.builder()
                .id(entity.getEdgeId())
                .sourceNodeId(entity.getSourceNodeId())
                .targetNodeId(entity.getTargetNodeId())
                .edgeType(entity.getEdgeType())
                .config(entity.getConfig() == null ? Collections.emptyMap() : entity.getConfig())
                .build();
    }
}
