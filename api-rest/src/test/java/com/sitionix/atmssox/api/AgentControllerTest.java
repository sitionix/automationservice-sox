package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    private AgentController agentController;

    @Mock
    private CreateAgent createAgent;

    @Mock
    private GetAgents getAgents;

    @Mock
    private GetAgent getAgent;

    @Mock
    private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentController = new AgentController(this.createAgent, this.getAgents, this.getAgent, this.agentApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.createAgent, this.getAgents, this.getAgent, this.agentApiMapper);
    }

    @Test
    void givenCreateAgentRequestDto_whenCreateAgent_thenReturnCreatedAgentDto() {
        //given
        final CreateAgentRequestDTO given = mock(CreateAgentRequestDTO.class);
        final CreateAgentCommand createAgentCommand = mock(CreateAgentCommand.class);
        final com.sitionix.atmssox.domain.model.Agent agent = mock(com.sitionix.atmssox.domain.model.Agent.class);
        final AgentDTO expected = mock(AgentDTO.class);

        when(this.agentApiMapper.asCreateAgentCommand(given)).thenReturn(createAgentCommand);
        when(this.createAgent.execute(createAgentCommand)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(expected);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.createAgent(given);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(expected));
        verify(this.agentApiMapper).asCreateAgentCommand(given);
        verify(this.createAgent).execute(createAgentCommand);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenNoInput_whenGetAgents_thenReturnAgentsResponseDto() {
        //given
        final List<com.sitionix.atmssox.domain.model.Agent> agents =
                List.of(mock(com.sitionix.atmssox.domain.model.Agent.class));
        final AgentsResponseDTO expected = mock(AgentsResponseDTO.class);

        when(this.getAgents.execute()).thenReturn(agents);
        when(this.agentApiMapper.asAgentsResponseDto(agents)).thenReturn(expected);

        //when
        final ResponseEntity<AgentsResponseDTO> actual = this.agentController.getAgents();

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.getAgents).execute();
        verify(this.agentApiMapper).asAgentsResponseDto(agents);
    }

    @Test
    void givenAgentId_whenGetAgent_thenReturnAgentDto() {
        //given
        final UUID given = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final com.sitionix.atmssox.domain.model.Agent agent = mock(com.sitionix.atmssox.domain.model.Agent.class);
        final AgentDTO expected = mock(AgentDTO.class);

        when(this.getAgent.execute(given)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(expected);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.getAgent(given);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.getAgent).execute(given);
        verify(this.agentApiMapper).asAgentDto(agent);
    }
}
