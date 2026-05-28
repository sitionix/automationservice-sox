package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.model.AgentProjectFlowEdge;
import com.sitionix.atmssox.domain.model.AgentProjectFlowNode;
import com.sitionix.atmssox.domain.repository.AgentProjectFlowRepository;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowEdgeJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowNodeJpaRepository;
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
                .map(this::asAgentProjectFlow);
    }

    private AgentProjectFlow asAgentProjectFlow(final AgentProjectFlowEntity flowEntity) {
        final List<AgentProjectFlowNode> nodes = this.agentProjectFlowNodeJpaRepository.findByFlowFlowIdOrderByCreatedAtAsc(flowEntity.getFlowId())
                .stream()
                .map(node -> AgentProjectFlowNode.builder()
                        .id(node.getNodeId())
                        .nodeType(node.getNodeType())
                        .referenceId(node.getReferenceId())
                        .positionX(node.getPositionX())
                        .positionY(node.getPositionY())
                        .designStatus(node.getDesignStatus())
                        .config(null)
                        .build())
                .toList();

        final List<AgentProjectFlowEdge> edges = this.agentProjectFlowEdgeJpaRepository.findByFlowFlowIdOrderByCreatedAtAsc(flowEntity.getFlowId())
                .stream()
                .map(edge -> AgentProjectFlowEdge.builder()
                        .id(edge.getEdgeId())
                        .sourceNodeId(edge.getSourceNodeId())
                        .targetNodeId(edge.getTargetNodeId())
                        .edgeType(edge.getEdgeType())
                        .config(null)
                        .build())
                .toList();

        return AgentProjectFlow.builder()
                .projectId(projectIdFrom(flowEntity))
                .flowId(flowEntity.getFlowId())
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private static UUID projectIdFrom(final AgentProjectFlowEntity flowEntity) {
        return flowEntity.getProject().getProjectId();
    }
}
