package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.model.AgentProjectFlowEdge;
import com.sitionix.atmssox.domain.model.AgentProjectFlowNode;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEdgeEntity;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowEntity;
import com.sitionix.atmssox.postgresql.entity.flow.AgentProjectFlowNodeEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowEdgeJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowJpaRepository;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectFlowNodeJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProjectFlowRepositoryImplTest {

    private AgentProjectFlowRepositoryImpl repository;

    @Mock
    private AgentProjectFlowJpaRepository agentProjectFlowJpaRepository;
    @Mock
    private AgentProjectFlowNodeJpaRepository agentProjectFlowNodeJpaRepository;
    @Mock
    private AgentProjectFlowEdgeJpaRepository agentProjectFlowEdgeJpaRepository;

    @BeforeEach
    void setUp() {
        this.repository = new AgentProjectFlowRepositoryImpl(this.agentProjectFlowJpaRepository, this.agentProjectFlowNodeJpaRepository,
                this.agentProjectFlowEdgeJpaRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectFlowJpaRepository, this.agentProjectFlowNodeJpaRepository,
                this.agentProjectFlowEdgeJpaRepository);
    }

    @Test
    void givenPersistedFlowWithNodesAndEdges_whenFindByProjectId_thenReturnMappedFlow() {
        //given
        final UUID projectId = UUID.fromString("e23fb6e5-c7f2-4a44-bf11-24443c97ece7");
        final UUID flowId = UUID.fromString("44296217-b3c6-48bb-b5af-31aad92321ec");
        final UUID nodeId = UUID.fromString("b9d6227f-d14c-4f43-a67c-6bdcf24045b8");
        final UUID edgeId = UUID.fromString("09864b56-6655-49d4-ac8e-4eb4ba85562e");
        final AgentProjectFlowEntity flowEntity = this.getFlowEntity(projectId, flowId);
        final AgentProjectFlowNodeEntity nodeEntity = new AgentProjectFlowNodeEntity(nodeId, flowEntity, "USER", null,
                10.0, 20.0, "ACTIVE", Instant.now(), Instant.now());
        final AgentProjectFlowEdgeEntity edgeEntity = new AgentProjectFlowEdgeEntity(edgeId, flowEntity, nodeId, nodeId,
                "LINK", Instant.now(), Instant.now());
        when(this.agentProjectFlowJpaRepository.findByProjectProjectId(projectId)).thenReturn(Optional.of(flowEntity));
        when(this.agentProjectFlowNodeJpaRepository.findByFlowFlowIdOrderByCreatedAtAsc(flowId)).thenReturn(List.of(nodeEntity));
        when(this.agentProjectFlowEdgeJpaRepository.findByFlowFlowIdOrderByCreatedAtAsc(flowId)).thenReturn(List.of(edgeEntity));

        //when
        final Optional<AgentProjectFlow> actual = this.repository.findByProjectId(projectId);

        //then
        assertThat(actual).contains(AgentProjectFlow.builder()
                .projectId(projectId)
                .flowId(flowId)
                .nodes(List.of(AgentProjectFlowNode.builder()
                        .id(nodeId)
                        .nodeType("USER")
                        .referenceId(null)
                        .positionX(10.0)
                        .positionY(20.0)
                        .designStatus("ACTIVE")
                        .config(null)
                        .build()))
                .edges(List.of(AgentProjectFlowEdge.builder()
                        .id(edgeId)
                        .sourceNodeId(nodeId)
                        .targetNodeId(nodeId)
                        .edgeType("LINK")
                        .config(null)
                        .build()))
                .build());
        verify(this.agentProjectFlowJpaRepository).findByProjectProjectId(projectId);
        verify(this.agentProjectFlowNodeJpaRepository).findByFlowFlowIdOrderByCreatedAtAsc(flowId);
        verify(this.agentProjectFlowEdgeJpaRepository).findByFlowFlowIdOrderByCreatedAtAsc(flowId);
    }

    @Test
    void givenNoFlowForProject_whenFindByProjectId_thenReturnEmptyOptional() {
        //given
        final UUID projectId = UUID.fromString("b4f38017-b148-4e0f-906f-cd9224bf6751");
        when(this.agentProjectFlowJpaRepository.findByProjectProjectId(projectId)).thenReturn(Optional.empty());

        //when
        final Optional<AgentProjectFlow> actual = this.repository.findByProjectId(projectId);

        //then
        assertThat(actual).isEmpty();
        verify(this.agentProjectFlowJpaRepository).findByProjectProjectId(projectId);
    }

    private AgentProjectFlowEntity getFlowEntity(final UUID projectId, final UUID flowId) {
        final AgentProjectEntity projectEntity = new AgentProjectEntity(projectId, 17L, "name", null, null, null,
                Instant.now(), Instant.now());
        return new AgentProjectFlowEntity(flowId, projectEntity, Instant.now(), Instant.now());
    }
}
