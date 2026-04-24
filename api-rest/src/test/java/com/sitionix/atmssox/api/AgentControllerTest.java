package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.atmssox.domain.usecase.ChatAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import com.sitionix.atmssox.domain.usecase.GetAgentRules;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
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
    private CreateAgentRule createAgentRule;

    @Mock
    private GetAgentRules getAgentRules;

    @Mock
    private PatchAgentRule patchAgentRule;

    @Mock
    private DeleteAgentRule deleteAgentRule;

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
                this.createAgentRule,
                this.getAgentRules,
                this.patchAgentRule,
                this.deleteAgentRule,
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
                this.createAgentRule,
                this.getAgentRules,
                this.patchAgentRule,
                this.deleteAgentRule,
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

    @Test
    void givenAgentId_whenGetAgentRules_thenReturnAgentRulesResponseDto() {
        //given
        final UUID agentId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        final List<AgentRule> rules = List.of(mock(AgentRule.class));
        final AgentRulesResponseDTO expected = mock(AgentRulesResponseDTO.class);

        when(this.getAgentRules.execute(agentId)).thenReturn(rules);
        when(this.agentApiMapper.asAgentRulesResponseDto(rules)).thenReturn(expected);

        //when
        final ResponseEntity<AgentRulesResponseDTO> actual = this.agentController.getAgentRules(agentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.getAgentRules).execute(agentId);
        verify(this.agentApiMapper).asAgentRulesResponseDto(rules);
    }

    @Test
    void givenCreateAgentRuleRequest_whenCreateAgentRule_thenReturnCreatedRuleDto() {
        //given
        final UUID agentId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        final CreateAgentRuleRequestDTO request = mock(CreateAgentRuleRequestDTO.class);
        final CreateAgentRuleCommand command = mock(CreateAgentRuleCommand.class);
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO expected = mock(AgentRuleDTO.class);

        when(this.agentApiMapper.asCreateAgentRuleCommand(request)).thenReturn(command);
        when(this.createAgentRule.execute(agentId, command)).thenReturn(rule);
        when(this.agentApiMapper.asAgentRuleDto(rule)).thenReturn(expected);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentController.createAgentRule(agentId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(expected));
        verify(this.agentApiMapper).asCreateAgentRuleCommand(request);
        verify(this.createAgentRule).execute(agentId, command);
        verify(this.agentApiMapper).asAgentRuleDto(rule);
    }

    @Test
    void givenPatchAgentRuleRequest_whenPatchAgentRule_thenReturnUpdatedRuleDto() {
        //given
        final UUID agentId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        final UUID ruleId = UUID.fromString("88888888-8888-8888-9999-888888888888");
        final PatchAgentRuleRequestDTO request = mock(PatchAgentRuleRequestDTO.class);
        final PatchAgentRuleCommand command = mock(PatchAgentRuleCommand.class);
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO expected = mock(AgentRuleDTO.class);

        when(this.agentApiMapper.asPatchAgentRuleCommand(request)).thenReturn(command);
        when(this.patchAgentRule.execute(agentId, ruleId, command)).thenReturn(rule);
        when(this.agentApiMapper.asAgentRuleDto(rule)).thenReturn(expected);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentController.patchAgentRule(agentId, ruleId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.agentApiMapper).asPatchAgentRuleCommand(request);
        verify(this.patchAgentRule).execute(agentId, ruleId, command);
        verify(this.agentApiMapper).asAgentRuleDto(rule);
    }

    @Test
    void givenAgentAndRuleIds_whenDeleteAgentRule_thenReturnDeleteResponseDto() {
        //given
        final UUID agentId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        final UUID ruleId = UUID.fromString("99999999-9999-9999-8888-999999999999");
        final DeleteAgentRuleResponse response = mock(DeleteAgentRuleResponse.class);
        final DeleteAgentRuleResponseDTO expected = mock(DeleteAgentRuleResponseDTO.class);

        when(this.deleteAgentRule.execute(agentId, ruleId)).thenReturn(response);
        when(this.agentApiMapper.asDeleteAgentRuleResponseDto(response)).thenReturn(expected);

        //when
        final ResponseEntity<DeleteAgentRuleResponseDTO> actual = this.agentController.deleteAgentRule(agentId, ruleId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(expected));
        verify(this.deleteAgentRule).execute(agentId, ruleId);
        verify(this.agentApiMapper).asDeleteAgentRuleResponseDto(response);
    }
}
