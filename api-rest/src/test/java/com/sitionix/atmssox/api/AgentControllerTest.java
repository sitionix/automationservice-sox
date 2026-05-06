package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
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
import java.util.UUID;
import java.util.List;

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

    @Test
    void givenNoInput_whenGetAgents_thenReturnAgentsResponseDto() {
        //given
        final List<Agent> agents = mock(List.class);
        final AgentsResponseDTO response = mock(AgentsResponseDTO.class);
        when(this.getAgents.execute()).thenReturn(agents);
        when(this.agentApiMapper.asAgentsResponseDto(agents)).thenReturn(response);

        //when
        final ResponseEntity<AgentsResponseDTO> actual = this.agentController.getAgents();

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.getAgents).execute();
        verify(this.agentApiMapper).asAgentsResponseDto(agents);
    }

    @Test
    void givenAgentId_whenGetAgent_thenReturnAgentDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final Agent agent = mock(Agent.class);
        final AgentDTO response = mock(AgentDTO.class);
        when(this.getAgent.execute(agentId)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(response);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.getAgent(agentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.getAgent).execute(agentId);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenPatchRequest_whenPatchAgent_thenReturnPatchedAgentDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final PatchAgentRequestDTO request = mock(PatchAgentRequestDTO.class);
        final PatchAgentCommand command = mock(PatchAgentCommand.class);
        final Agent agent = mock(Agent.class);
        final AgentDTO response = mock(AgentDTO.class);
        when(this.agentApiMapper.asPatchAgentCommand(request)).thenReturn(command);
        when(this.patchAgent.execute(agentId, command)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(response);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.patchAgent(agentId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.agentApiMapper).asPatchAgentCommand(request);
        verify(this.patchAgent).execute(agentId, command);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenAgentId_whenActivateArchiveRestoreDeleteAgent_thenReturnAgentDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final Agent activated = mock(Agent.class);
        final Agent archived = mock(Agent.class);
        final Agent restored = mock(Agent.class);
        final Agent deleted = mock(Agent.class);
        final AgentDTO activatedDto = mock(AgentDTO.class);
        final AgentDTO archivedDto = mock(AgentDTO.class);
        final AgentDTO restoredDto = mock(AgentDTO.class);
        final AgentDTO deletedDto = mock(AgentDTO.class);
        when(this.activateAgent.execute(agentId)).thenReturn(activated);
        when(this.archiveAgent.execute(agentId)).thenReturn(archived);
        when(this.restoreAgent.execute(agentId)).thenReturn(restored);
        when(this.deleteAgent.execute(agentId)).thenReturn(deleted);
        when(this.agentApiMapper.asAgentDto(activated)).thenReturn(activatedDto);
        when(this.agentApiMapper.asAgentDto(archived)).thenReturn(archivedDto);
        when(this.agentApiMapper.asAgentDto(restored)).thenReturn(restoredDto);
        when(this.agentApiMapper.asAgentDto(deleted)).thenReturn(deletedDto);

        //when
        final ResponseEntity<AgentDTO> activateResponse = this.agentController.activateAgent(agentId);
        final ResponseEntity<AgentDTO> archiveResponse = this.agentController.archiveAgent(agentId);
        final ResponseEntity<AgentDTO> restoreResponse = this.agentController.restoreAgent(agentId);
        final ResponseEntity<AgentDTO> deleteResponse = this.agentController.deleteAgent(agentId);

        //then
        assertThat(activateResponse).isEqualTo(ResponseEntity.ok(activatedDto));
        assertThat(archiveResponse).isEqualTo(ResponseEntity.ok(archivedDto));
        assertThat(restoreResponse).isEqualTo(ResponseEntity.ok(restoredDto));
        assertThat(deleteResponse).isEqualTo(ResponseEntity.ok(deletedDto));
        verify(this.activateAgent).execute(agentId);
        verify(this.archiveAgent).execute(agentId);
        verify(this.restoreAgent).execute(agentId);
        verify(this.deleteAgent).execute(agentId);
        verify(this.agentApiMapper).asAgentDto(activated);
        verify(this.agentApiMapper).asAgentDto(archived);
        verify(this.agentApiMapper).asAgentDto(restored);
        verify(this.agentApiMapper).asAgentDto(deleted);
    }
}
