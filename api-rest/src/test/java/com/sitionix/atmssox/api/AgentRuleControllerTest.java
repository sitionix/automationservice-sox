package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleAuthorTypeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleStatusDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentRuleApiMapper;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.GetAgentRulesQuery;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import com.sitionix.atmssox.domain.usecase.AcceptAgentRule;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import com.sitionix.atmssox.domain.usecase.GetAgentRules;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
import com.sitionix.atmssox.domain.usecase.RejectAgentRule;
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
class AgentRuleControllerTest {

    private AgentRuleController agentRuleController;

    @Mock private CreateAgentRule createAgentRule;
    @Mock private GetAgentRules getAgentRules;
    @Mock private PatchAgentRule patchAgentRule;
    @Mock private DeleteAgentRule deleteAgentRule;
    @Mock private AcceptAgentRule acceptAgentRule;
    @Mock private RejectAgentRule rejectAgentRule;
    @Mock private AgentRuleApiMapper agentRuleApiMapper;

    @BeforeEach
    void setUp() {
        this.agentRuleController = new AgentRuleController(this.createAgentRule, this.getAgentRules, this.patchAgentRule,
                this.deleteAgentRule, this.acceptAgentRule, this.rejectAgentRule, this.agentRuleApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.createAgentRule, this.getAgentRules, this.patchAgentRule, this.deleteAgentRule,
                this.acceptAgentRule, this.rejectAgentRule, this.agentRuleApiMapper);
    }

    @Test
    void givenAgentIdAndFilters_whenGetAgentRules_thenReturnRulesResponseDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final AgentRuleStatusDTO status = mock(AgentRuleStatusDTO.class);
        final AgentRuleAuthorTypeDTO authorType = null;
        final GetAgentRulesQuery query = mock(GetAgentRulesQuery.class);
        final java.util.List<AgentRule> rules = mock(java.util.List.class);
        final AgentRulesResponseDTO response = mock(AgentRulesResponseDTO.class);
        when(this.agentRuleApiMapper.asGetAgentRulesQuery(status, null)).thenReturn(query);
        when(this.getAgentRules.execute(agentId, query)).thenReturn(rules);
        when(this.agentRuleApiMapper.asAgentRulesResponseDto(rules)).thenReturn(response);

        //when
        final ResponseEntity<AgentRulesResponseDTO> actual = this.agentRuleController.getAgentRules(agentId, status, null);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.agentRuleApiMapper).asGetAgentRulesQuery(status, null);
        verify(this.getAgentRules).execute(agentId, query);
        verify(this.agentRuleApiMapper).asAgentRulesResponseDto(rules);
    }

    @Test
    void givenCreateRequest_whenCreateAgentRule_thenReturnCreatedRuleDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final CreateAgentRuleRequestDTO request = mock(CreateAgentRuleRequestDTO.class);
        final CreateAgentRuleCommand command = mock(CreateAgentRuleCommand.class);
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO response = mock(AgentRuleDTO.class);
        when(this.agentRuleApiMapper.asCreateAgentRuleCommand(request)).thenReturn(command);
        when(this.createAgentRule.execute(agentId, command)).thenReturn(rule);
        when(this.agentRuleApiMapper.asAgentRuleDto(rule)).thenReturn(response);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentRuleController.createAgentRule(agentId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(response));
        verify(this.agentRuleApiMapper).asCreateAgentRuleCommand(request);
        verify(this.createAgentRule).execute(agentId, command);
        verify(this.agentRuleApiMapper).asAgentRuleDto(rule);
    }

    @Test
    void givenPatchRequest_whenPatchAgentRule_thenReturnPatchedRuleDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final UUID ruleId = UUID.randomUUID();
        final PatchAgentRuleRequestDTO request = mock(PatchAgentRuleRequestDTO.class);
        final PatchAgentRuleCommand command = mock(PatchAgentRuleCommand.class);
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO response = mock(AgentRuleDTO.class);
        when(this.agentRuleApiMapper.asPatchAgentRuleCommand(request)).thenReturn(command);
        when(this.patchAgentRule.execute(agentId, ruleId, command)).thenReturn(rule);
        when(this.agentRuleApiMapper.asAgentRuleDto(rule)).thenReturn(response);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentRuleController.patchAgentRule(agentId, ruleId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.agentRuleApiMapper).asPatchAgentRuleCommand(request);
        verify(this.patchAgentRule).execute(agentId, ruleId, command);
        verify(this.agentRuleApiMapper).asAgentRuleDto(rule);
    }

    @Test
    void givenRuleId_whenDeleteAgentRule_thenReturnDeleteResponseDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final UUID ruleId = UUID.randomUUID();
        final DeleteAgentRuleResponse deleteResponse = mock(DeleteAgentRuleResponse.class);
        final DeleteAgentRuleResponseDTO response = mock(DeleteAgentRuleResponseDTO.class);
        when(this.deleteAgentRule.execute(agentId, ruleId)).thenReturn(deleteResponse);
        when(this.agentRuleApiMapper.asDeleteAgentRuleResponseDto(deleteResponse)).thenReturn(response);

        //when
        final ResponseEntity<DeleteAgentRuleResponseDTO> actual = this.agentRuleController.deleteAgentRule(agentId, ruleId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.deleteAgentRule).execute(agentId, ruleId);
        verify(this.agentRuleApiMapper).asDeleteAgentRuleResponseDto(deleteResponse);
    }

    @Test
    void givenAcceptRequest_whenAcceptAgentRule_thenReturnAcceptedRuleDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final UUID ruleId = UUID.randomUUID();
        final AcceptAgentRuleRequestDTO request = mock(AcceptAgentRuleRequestDTO.class);
        final AcceptAgentRuleCommand command = mock(AcceptAgentRuleCommand.class);
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO response = mock(AgentRuleDTO.class);
        when(this.agentRuleApiMapper.asAcceptAgentRuleCommand(request)).thenReturn(command);
        when(this.acceptAgentRule.execute(agentId, ruleId, command)).thenReturn(rule);
        when(this.agentRuleApiMapper.asAgentRuleDto(rule)).thenReturn(response);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentRuleController.acceptAgentRule(agentId, ruleId, request);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.agentRuleApiMapper).asAcceptAgentRuleCommand(request);
        verify(this.acceptAgentRule).execute(agentId, ruleId, command);
        verify(this.agentRuleApiMapper).asAgentRuleDto(rule);
    }

    @Test
    void givenAgentAndRuleId_whenRejectAgentRule_thenReturnRejectedRuleDto() {
        //given
        final UUID agentId = UUID.randomUUID();
        final UUID ruleId = UUID.randomUUID();
        final AgentRule rule = mock(AgentRule.class);
        final AgentRuleDTO response = mock(AgentRuleDTO.class);
        when(this.rejectAgentRule.execute(agentId, ruleId)).thenReturn(rule);
        when(this.agentRuleApiMapper.asAgentRuleDto(rule)).thenReturn(response);

        //when
        final ResponseEntity<AgentRuleDTO> actual = this.agentRuleController.rejectAgentRule(agentId, ruleId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.rejectAgentRule).execute(agentId, ruleId);
        verify(this.agentRuleApiMapper).asAgentRuleDto(rule);
    }
}
