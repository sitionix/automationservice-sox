package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AddAgentToProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentsResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentProjectApiMapper;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.usecase.AddAgentToProject;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import com.sitionix.atmssox.domain.usecase.DeleteAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectAgents;
import com.sitionix.atmssox.domain.usecase.GetAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
import com.sitionix.atmssox.domain.usecase.PatchAgentProject;
import com.sitionix.atmssox.domain.usecase.RemoveAgentFromProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProjectControllerTest {

    private AgentProjectController agentProjectController;

    @Mock private CreateAgentProject createAgentProject;
    @Mock private GetAgentProjects getAgentProjects;
    @Mock private GetAgentProject getAgentProject;
    @Mock private PatchAgentProject patchAgentProject;
    @Mock private DeleteAgentProject deleteAgentProject;
    @Mock private GetAgentProjectAgents getAgentProjectAgents;
    @Mock private AddAgentToProject addAgentToProject;
    @Mock private RemoveAgentFromProject removeAgentFromProject;
    @Mock private AgentApiMapper agentApiMapper;
    @Mock private AgentProjectApiMapper agentProjectApiMapper;

    @BeforeEach
    void setUp() {
        this.agentProjectController = new AgentProjectController(this.createAgentProject, this.getAgentProjects, this.getAgentProject,
                this.patchAgentProject, this.deleteAgentProject, this.getAgentProjectAgents, this.addAgentToProject, this.removeAgentFromProject,
                this.agentApiMapper, this.agentProjectApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.createAgentProject, this.getAgentProjects, this.getAgentProject, this.patchAgentProject,
                this.deleteAgentProject, this.getAgentProjectAgents, this.addAgentToProject, this.removeAgentFromProject, this.agentApiMapper,
                this.agentProjectApiMapper);
    }

    @Test
    void givenCreateAgentProjectRequestDto_whenCreateAgentProject_thenReturnCreatedProjectDto() {
        //given
        final CreateAgentProjectRequestDTO request = mock(CreateAgentProjectRequestDTO.class);
        final CreateAgentProjectCommand command = mock(CreateAgentProjectCommand.class);
        final AgentProject project = mock(AgentProject.class);
        final AgentProjectDTO response = mock(AgentProjectDTO.class);
        when(this.agentApiMapper.asCreateAgentProjectCommand(request)).thenReturn(command);
        when(this.createAgentProject.execute(command)).thenReturn(project);
        when(this.agentProjectApiMapper.asAgentProjectDto(project)).thenReturn(response);

        //when
        final ResponseEntity<AgentProjectDTO> actual = this.agentProjectController.createAgentProject(request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(response));
        verify(this.agentApiMapper).asCreateAgentProjectCommand(request);
        verify(this.createAgentProject).execute(command);
        verify(this.agentProjectApiMapper).asAgentProjectDto(project);
    }

    @Test
    void givenPageAndSize_whenGetAgentProjects_thenReturnProjectsPageResponseDto() {
        //given
        final AgentProjectsPage pageResponse = mock(AgentProjectsPage.class);
        final AgentProjectsPageResponseDTO responseDto = mock(AgentProjectsPageResponseDTO.class);
        when(this.getAgentProjects.execute(org.mockito.ArgumentMatchers.any(GetAgentProjectsQuery.class))).thenReturn(pageResponse);
        when(this.agentProjectApiMapper.asAgentProjectsPageResponseDto(pageResponse)).thenReturn(responseDto);

        //when
        final ResponseEntity<AgentProjectsPageResponseDTO> actual = this.agentProjectController.getAgentProjects(1, 20);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.getAgentProjects).execute(org.mockito.ArgumentMatchers.any(GetAgentProjectsQuery.class));
        verify(this.agentProjectApiMapper).asAgentProjectsPageResponseDto(pageResponse);
    }

    @Test
    void givenProjectId_whenGetAgentProject_thenReturnProjectDto() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();
        final AgentProject project = mock(AgentProject.class);
        final AgentProjectDTO responseDto = mock(AgentProjectDTO.class);
        when(this.getAgentProject.execute(projectId)).thenReturn(project);
        when(this.agentProjectApiMapper.asAgentProjectDto(project)).thenReturn(responseDto);

        //when
        final ResponseEntity<AgentProjectDTO> actual = this.agentProjectController.getAgentProject(projectId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.getAgentProject).execute(projectId);
        verify(this.agentProjectApiMapper).asAgentProjectDto(project);
    }

    @Test
    void givenPatchRequestDto_whenPatchAgentProject_thenReturnPatchedProjectDto() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();
        final PatchAgentProjectRequestDTO requestDto = mock(PatchAgentProjectRequestDTO.class);
        final PatchAgentProjectCommand command = mock(PatchAgentProjectCommand.class);
        final AgentProject response = mock(AgentProject.class);
        final AgentProjectDTO responseDto = mock(AgentProjectDTO.class);
        when(this.agentProjectApiMapper.asPatchAgentProjectCommand(requestDto)).thenReturn(command);
        when(this.patchAgentProject.execute(projectId, command)).thenReturn(response);
        when(this.agentProjectApiMapper.asAgentProjectDto(response)).thenReturn(responseDto);

        //when
        final ResponseEntity<AgentProjectDTO> actual = this.agentProjectController.patchAgentProject(projectId, requestDto);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.agentProjectApiMapper).asPatchAgentProjectCommand(requestDto);
        verify(this.patchAgentProject).execute(projectId, command);
        verify(this.agentProjectApiMapper).asAgentProjectDto(response);
    }

    @Test
    void givenProjectId_whenDeleteAgentProject_thenReturnNoContent() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();

        //when
        final ResponseEntity<Void> actual = this.agentProjectController.deleteAgentProject(projectId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.noContent().build());
        verify(this.deleteAgentProject).execute(projectId);
    }

    @Test
    void givenProjectId_whenListAgentProjectAgents_thenReturnProjectAgentsResponseDto() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();
        final java.util.List<ProjectAgent> response = java.util.List.of(mock(ProjectAgent.class));
        final ProjectAgentsResponseDTO responseDto = mock(ProjectAgentsResponseDTO.class);
        when(this.getAgentProjectAgents.execute(projectId)).thenReturn(response);
        when(this.agentProjectApiMapper.asProjectAgentsResponseDto(response)).thenReturn(responseDto);

        //when
        final ResponseEntity<ProjectAgentsResponseDTO> actual = this.agentProjectController.listAgentProjectAgents(projectId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.getAgentProjectAgents).execute(projectId);
        verify(this.agentProjectApiMapper).asProjectAgentsResponseDto(response);
    }

    @Test
    void givenAddAgentRequest_whenAddAgentToProject_thenReturnProjectAgentResponseDto() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();
        final java.util.UUID agentId = java.util.UUID.randomUUID();
        final AddAgentToProjectRequestDTO request = mock(AddAgentToProjectRequestDTO.class);
        final ProjectAgent projectAgent = mock(ProjectAgent.class);
        final ProjectAgentResponseDTO responseDto = mock(ProjectAgentResponseDTO.class);
        when(this.agentProjectApiMapper.asAgentId(request)).thenReturn(agentId);
        when(this.addAgentToProject.execute(projectId, agentId)).thenReturn(projectAgent);
        when(this.agentProjectApiMapper.asProjectAgentResponseDto(projectAgent)).thenReturn(responseDto);

        //when
        final ResponseEntity<ProjectAgentResponseDTO> actual = this.agentProjectController.addAgentToProject(projectId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.agentProjectApiMapper).asAgentId(request);
        verify(this.addAgentToProject).execute(projectId, agentId);
        verify(this.agentProjectApiMapper).asProjectAgentResponseDto(projectAgent);
    }

    @Test
    void givenProjectIdAndAgentId_whenRemoveAgentFromProject_thenReturnNoContent() {
        //given
        final java.util.UUID projectId = java.util.UUID.randomUUID();
        final java.util.UUID agentId = java.util.UUID.randomUUID();

        //when
        final ResponseEntity<Void> actual = this.agentProjectController.removeAgentFromProject(projectId, agentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.noContent().build());
        verify(this.removeAgentFromProject).execute(projectId, agentId);
    }
}
