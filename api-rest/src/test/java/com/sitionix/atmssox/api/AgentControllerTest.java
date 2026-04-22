package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.atmssox.domain.usecase.ChatAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.DeleteAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
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
    private GetAgentConversations getAgentConversations;

    @Mock
    private GetAgentConversation getAgentConversation;

    @Mock
    private PatchAgent patchAgent;

    @Mock
    private ActivateAgent activateAgent;

    @Mock
    private ArchiveAgent archiveAgent;

    @Mock
    private ChatAgent chatAgent;

    @Mock
    private RestoreAgent restoreAgent;

    @Mock
    private DeleteAgent deleteAgent;

    @Mock
    private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentController = new AgentController(
                this.createAgent,
                this.getAgents,
                this.getAgent,
                this.getAgentConversations,
                this.getAgentConversation,
                this.patchAgent,
                this.activateAgent,
                this.archiveAgent,
                this.chatAgent,
                this.restoreAgent,
                this.deleteAgent,
                this.agentApiMapper
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.createAgent,
                this.getAgents,
                this.getAgent,
                this.getAgentConversations,
                this.getAgentConversation,
                this.patchAgent,
                this.activateAgent,
                this.archiveAgent,
                this.chatAgent,
                this.restoreAgent,
                this.deleteAgent,
                this.agentApiMapper
        );
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

    @Test
    void givenAgentId_whenGetAgentConversations_thenReturnAgentConversationsResponseDto() {
        //given
        final UUID given = UUID.fromString("11111111-1111-1111-1111-111111111112");
        final List<Conversation> conversations = List.of(mock(Conversation.class));
        final AgentConversationsResponseDTO expected = mock(AgentConversationsResponseDTO.class);

        when(this.getAgentConversations.execute(given)).thenReturn(conversations);
        when(this.agentApiMapper.asAgentConversationsResponseDto(conversations)).thenReturn(expected);

        //when
        final ResponseEntity<AgentConversationsResponseDTO> actual = this.agentController.getAgentConversations(given);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.getAgentConversations).execute(given);
        verify(this.agentApiMapper).asAgentConversationsResponseDto(conversations);
    }

    @Test
    void givenConversationId_whenGetAgentConversation_thenReturnAgentConversationDetailsDto() {
        //given
        final UUID givenConversationId = UUID.fromString("11111111-1111-1111-1111-111111111114");
        final ConversationDetails details = mock(ConversationDetails.class);
        final AgentConversationDetailsDTO expected = mock(AgentConversationDetailsDTO.class);

        when(this.getAgentConversation.execute(givenConversationId)).thenReturn(details);
        when(this.agentApiMapper.asAgentConversationDetailsDto(details)).thenReturn(expected);

        //when
        final ResponseEntity<AgentConversationDetailsDTO> actual =
                this.agentController.getAgentConversation(givenConversationId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.getAgentConversation).execute(givenConversationId);
        verify(this.agentApiMapper).asAgentConversationDetailsDto(details);
    }

    @Test
    void givenPatchAgentRequestDto_whenPatchAgent_thenReturnAgentDto() {
        //given
        final UUID givenAgentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final PatchAgentRequestDTO givenRequest = mock(PatchAgentRequestDTO.class);
        final PatchAgentCommand givenCommand = mock(PatchAgentCommand.class);
        final com.sitionix.atmssox.domain.model.Agent agent = mock(com.sitionix.atmssox.domain.model.Agent.class);
        final AgentDTO expected = mock(AgentDTO.class);

        when(this.agentApiMapper.asPatchAgentCommand(givenRequest)).thenReturn(givenCommand);
        when(this.patchAgent.execute(givenAgentId, givenCommand)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(expected);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.patchAgent(givenAgentId, givenRequest);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.agentApiMapper).asPatchAgentCommand(givenRequest);
        verify(this.patchAgent).execute(givenAgentId, givenCommand);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenAgentId_whenActivateAgent_thenReturnAgentDto() {
        //given
        final UUID givenAgentId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        final com.sitionix.atmssox.domain.model.Agent agent = mock(com.sitionix.atmssox.domain.model.Agent.class);
        final AgentDTO expected = mock(AgentDTO.class);

        when(this.activateAgent.execute(givenAgentId)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(expected);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.activateAgent(givenAgentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.activateAgent).execute(givenAgentId);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenAgentId_whenArchiveAgent_thenReturnAgentDto() {
        //given
        final UUID givenAgentId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        final com.sitionix.atmssox.domain.model.Agent agent = mock(com.sitionix.atmssox.domain.model.Agent.class);
        final AgentDTO expected = mock(AgentDTO.class);

        when(this.archiveAgent.execute(givenAgentId)).thenReturn(agent);
        when(this.agentApiMapper.asAgentDto(agent)).thenReturn(expected);

        //when
        final ResponseEntity<AgentDTO> actual = this.agentController.archiveAgent(givenAgentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.archiveAgent).execute(givenAgentId);
        verify(this.agentApiMapper).asAgentDto(agent);
    }

    @Test
    void givenChatAgentRequestDto_whenChatAgent_thenReturnChatAgentResponseDto() {
        //given
        final UUID givenAgentId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        final ChatAgentRequestDTO givenRequest = mock(ChatAgentRequestDTO.class);
        final ChatAgentCommand command = mock(ChatAgentCommand.class);
        final ChatAgentResponse response = mock(ChatAgentResponse.class);
        final ChatAgentResponseDTO expected = mock(ChatAgentResponseDTO.class);

        when(this.agentApiMapper.asChatAgentCommand(givenRequest)).thenReturn(command);
        when(this.chatAgent.execute(givenAgentId, command)).thenReturn(response);
        when(this.agentApiMapper.asChatAgentResponseDto(response)).thenReturn(expected);

        //when
        final ResponseEntity<ChatAgentResponseDTO> actual = this.agentController.chatAgent(givenAgentId, givenRequest);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.agentApiMapper).asChatAgentCommand(givenRequest);
        verify(this.chatAgent).execute(givenAgentId, command);
        verify(this.agentApiMapper).asChatAgentResponseDto(response);
    }
}
