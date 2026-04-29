package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentRuleApiMapper;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.usecase.AcceptAgentRule;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgent;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgentChatExecution;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import com.sitionix.atmssox.domain.usecase.GetAgentRules;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import com.sitionix.atmssox.domain.usecase.PatchAgent;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
import com.sitionix.atmssox.domain.usecase.RejectAgentRule;
import com.sitionix.atmssox.domain.usecase.RestoreAgent;
import com.sitionix.atmssox.domain.usecase.SubmitAgentChatExecution;
import java.util.UUID;
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
    @Mock private GetAgentConversations getAgentConversations;
    @Mock private GetAgentConversation getAgentConversation;
    @Mock private PatchAgent patchAgent;
    @Mock private ActivateAgent activateAgent;
    @Mock private ArchiveAgent archiveAgent;
    @Mock private SubmitAgentChatExecution submitAgentChatExecution;
    @Mock private GetAgentChatExecution getAgentChatExecution;
    @Mock private CreateAgentRule createAgentRule;
    @Mock private GetAgentRules getAgentRules;
    @Mock private PatchAgentRule patchAgentRule;
    @Mock private DeleteAgentRule deleteAgentRule;
    @Mock private AcceptAgentRule acceptAgentRule;
    @Mock private RejectAgentRule rejectAgentRule;
    @Mock private RestoreAgent restoreAgent;
    @Mock private DeleteAgent deleteAgent;
    @Mock private AgentApiMapper agentApiMapper;
    @Mock private AgentRuleApiMapper agentRuleApiMapper;

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
                this.submitAgentChatExecution,
                this.getAgentChatExecution,
                this.createAgentRule,
                this.getAgentRules,
                this.patchAgentRule,
                this.deleteAgentRule,
                this.acceptAgentRule,
                this.rejectAgentRule,
                this.restoreAgent,
                this.deleteAgent,
                this.agentApiMapper,
                this.agentRuleApiMapper
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
                this.submitAgentChatExecution,
                this.getAgentChatExecution,
                this.createAgentRule,
                this.getAgentRules,
                this.patchAgentRule,
                this.deleteAgentRule,
                this.acceptAgentRule,
                this.rejectAgentRule,
                this.restoreAgent,
                this.deleteAgent,
                this.agentApiMapper,
                this.agentRuleApiMapper
        );
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
    void givenSubmitExecutionRequest_whenSubmitAgentChatExecution_thenReturnAcceptedEnvelope() {
        //given
        final UUID agentId = UUID.fromString("76f023a2-cb0c-44d5-970d-053f4af51f6b");
        final ChatAgentRequestDTO request = mock(ChatAgentRequestDTO.class);
        final ChatAgentCommand command = mock(ChatAgentCommand.class);
        final ChatExecution execution = mock(ChatExecution.class);
        final SubmitChatExecutionResponseDTO response = mock(SubmitChatExecutionResponseDTO.class);

        when(this.agentApiMapper.asChatAgentCommand(request)).thenReturn(command);
        when(this.submitAgentChatExecution.execute(agentId, command, "idem")).thenReturn(execution);
        when(this.agentApiMapper.asSubmitChatExecutionResponseDto(execution)).thenReturn(response);

        //when
        final ResponseEntity<SubmitChatExecutionResponseDTO> actual =
                this.agentController.submitAgentChatExecution(agentId, request, "idem");

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
        verify(this.agentApiMapper).asChatAgentCommand(request);
        verify(this.submitAgentChatExecution).execute(agentId, command, "idem");
        verify(this.agentApiMapper).asSubmitChatExecutionResponseDto(execution);
    }

    @Test
    void givenExecutionLookupRequest_whenGetAgentChatExecution_thenReturnOkEnvelope() {
        //given
        final UUID agentId = UUID.fromString("1f723177-ec03-4506-9011-cf0b39c97c61");
        final UUID executionId = UUID.fromString("d67d95cb-8fcb-4a09-8f17-4ad5295a784b");
        final UUID conversationId = UUID.fromString("3b08ad4e-13f6-4d83-ab5d-dfe04ccebe4f");
        final ChatExecution execution = mock(ChatExecution.class);
        final ChatExecutionDTO response = mock(ChatExecutionDTO.class);

        when(this.getAgentChatExecution.execute(agentId, executionId, conversationId)).thenReturn(execution);
        when(this.agentApiMapper.asChatExecutionDto(execution)).thenReturn(response);

        //when
        final ResponseEntity<ChatExecutionDTO> actual =
                this.agentController.getAgentChatExecution(agentId, executionId, conversationId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.getAgentChatExecution).execute(agentId, executionId, conversationId);
        verify(this.agentApiMapper).asChatExecutionDto(execution);
    }
}
