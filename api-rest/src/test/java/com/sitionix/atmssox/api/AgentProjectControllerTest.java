package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentProjectApiMapper;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
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
    @Mock private AgentApiMapper agentApiMapper;
    @Mock private AgentProjectApiMapper agentProjectApiMapper;

    @BeforeEach
    void setUp() {
        this.agentProjectController = new AgentProjectController(this.createAgentProject, this.getAgentProjects, this.getAgentProject,
                this.agentApiMapper, this.agentProjectApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.createAgentProject, this.getAgentProjects, this.getAgentProject, this.agentApiMapper, this.agentProjectApiMapper);
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
}
