package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.DeleteAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import com.sitionix.atmssox.domain.usecase.PatchAgent;
import com.sitionix.atmssox.domain.usecase.RestoreAgent;
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
class AgentControllerTest {

    private AgentController agentController;

    @Mock private CreateAgent createAgent;
    @Mock private GetAgents getAgents;
    @Mock private GetAgent getAgent;
    @Mock private PatchAgent patchAgent;
    @Mock private ActivateAgent activateAgent;
    @Mock private ArchiveAgent archiveAgent;
    @Mock private RestoreAgent restoreAgent;
    @Mock private DeleteAgent deleteAgent;
    @Mock private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentController = new AgentController(this.createAgent, this.getAgents, this.getAgent, this.patchAgent, this.activateAgent,
                this.archiveAgent, this.restoreAgent, this.deleteAgent, this.agentApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.createAgent, this.getAgents, this.getAgent, this.patchAgent, this.activateAgent,
                this.archiveAgent, this.restoreAgent, this.deleteAgent, this.agentApiMapper);
    }

    @Test
    void givenCreateAgentRequestDto_whenCreateAgent_thenReturnCreatedAgentDto() {
        //given
        final CreateAgentRequestDTO request = mock(CreateAgentRequestDTO.class);
        final CreateAgentCommand command = mock(CreateAgentCommand.class);
        final Agent agent = mock(Agent.class);
        final AgentDTO response = mock(AgentDTO.class);
        when(this.agentApiMapper.asCreateAgentCommand(request)).thenReturn(command);
        when(this.createAgent.execute(command)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(response);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.createAgent(request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(response));
        verify(this.agentApiMapper).asCreateAgentCommand(request);
        verify(this.createAgent).execute(command);
        verify(this.agentApiMapper).asAgentDto(agent);
    }
}
