package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectStatusEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProjectInfraMapperTest {

    private AgentProjectInfraMapper mapper;

    @Mock
    private AgentProjectStatusInfraMapper agentProjectStatusInfraMapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AgentProjectInfraMapperImpl(this.agentProjectStatusInfraMapper);
    }

    @Test
    void givenProject_whenAsAgentProjectEntity_thenReturnMappedEntity() {
        //given
        final AgentProject given = AgentProject.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .ownerUserId(17L)
                .name("Project")
                .description("Description")
                .context("Context")
                .status(AgentProjectStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-10T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-10T10:01:00Z"))
                .build();
        final AgentProjectStatusEntity statusEntity = AgentProjectStatusEntity.builder().id(1L).build();

        when(this.agentProjectStatusInfraMapper.asStatusEntity(given.getStatus())).thenReturn(statusEntity);

        //when
        final AgentProjectEntity actual = this.mapper.asAgentProjectEntity(given);

        //then
        assertThat(actual.getProjectId()).isEqualTo(given.getId());
        assertThat(actual.getOwnerUserId()).isEqualTo(given.getOwnerUserId());
        assertThat(actual.getName()).isEqualTo(given.getName());
        assertThat(actual.getDescription()).isEqualTo(given.getDescription());
        assertThat(actual.getContext()).isEqualTo(given.getContext());
        assertThat(actual.getStatus()).isEqualTo(statusEntity);
        assertThat(actual.getCreatedAt()).isEqualTo(given.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(given.getUpdatedAt());
    }

    @Test
    void givenEntity_whenAsAgentProject_thenReturnMappedProject() {
        //given
        final AgentProjectEntity given = new AgentProjectEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                17L,
                "Project",
                "Description",
                "Context",
                AgentProjectStatusEntity.builder().id(1L).description("ACTIVE").build(),
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-10T10:01:00Z")
        );

        when(this.agentProjectStatusInfraMapper.asStatus(given.getStatus())).thenReturn(AgentProjectStatus.ACTIVE);

        //when
        final AgentProject actual = this.mapper.asAgentProject(given);

        //then
        assertThat(actual.getId()).isEqualTo(given.getProjectId());
        assertThat(actual.getOwnerUserId()).isEqualTo(given.getOwnerUserId());
        assertThat(actual.getName()).isEqualTo(given.getName());
        assertThat(actual.getDescription()).isEqualTo(given.getDescription());
        assertThat(actual.getContext()).isEqualTo(given.getContext());
        assertThat(actual.getStatus()).isEqualTo(AgentProjectStatus.ACTIVE);
        assertThat(actual.getCreatedAt()).isEqualTo(given.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(given.getUpdatedAt());
    }
}
