package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentRuleApi;
import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleAuthorTypeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleStatusDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentRuleApiMapper;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.usecase.AcceptAgentRule;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import com.sitionix.atmssox.domain.usecase.GetAgentRules;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
import com.sitionix.atmssox.domain.usecase.RejectAgentRule;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentRuleController implements AgentRuleApi {

    private final CreateAgentRule createAgentRule;
    private final GetAgentRules getAgentRules;
    private final PatchAgentRule patchAgentRule;
    private final DeleteAgentRule deleteAgentRule;
    private final AcceptAgentRule acceptAgentRule;
    private final RejectAgentRule rejectAgentRule;
    private final AgentRuleApiMapper agentRuleApiMapper;

    @Override
    public ResponseEntity<AgentRulesResponseDTO> getAgentRules(final UUID agentId,
                                                               final AgentRuleStatusDTO status,
                                                               final AgentRuleAuthorTypeDTO authorType) {
        return ResponseEntity.ok(this.agentRuleApiMapper.asAgentRulesResponseDto(
                this.getAgentRules.execute(agentId, this.agentRuleApiMapper.asGetAgentRulesQuery(status, authorType))
        ));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> createAgentRule(final UUID agentId, @Valid final CreateAgentRuleRequestDTO createAgentRuleRequestDTO) {
        final CreateAgentRuleCommand command = this.agentRuleApiMapper.asCreateAgentRuleCommand(createAgentRuleRequestDTO);
        final AgentRule response = this.createAgentRule.execute(agentId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.agentRuleApiMapper.asAgentRuleDto(response));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> patchAgentRule(final UUID agentId,
                                                       final UUID ruleId,
                                                       @Valid final PatchAgentRuleRequestDTO patchAgentRuleRequestDTO) {
        final AgentRule response = this.patchAgentRule.execute(agentId, ruleId, this.agentRuleApiMapper.asPatchAgentRuleCommand(patchAgentRuleRequestDTO));
        return ResponseEntity.ok(this.agentRuleApiMapper.asAgentRuleDto(response));
    }

    @Override
    public ResponseEntity<DeleteAgentRuleResponseDTO> deleteAgentRule(final UUID agentId, final UUID ruleId) {
        return ResponseEntity.ok(this.agentRuleApiMapper.asDeleteAgentRuleResponseDto(this.deleteAgentRule.execute(agentId, ruleId)));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> acceptAgentRule(final UUID agentId,
                                                        final UUID ruleId,
                                                        @Valid final AcceptAgentRuleRequestDTO acceptAgentRuleRequestDTO) {
        final AcceptAgentRuleCommand command = this.agentRuleApiMapper.asAcceptAgentRuleCommand(acceptAgentRuleRequestDTO);
        final AgentRule response = this.acceptAgentRule.execute(agentId, ruleId, command);
        return ResponseEntity.ok(this.agentRuleApiMapper.asAgentRuleDto(response));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> rejectAgentRule(final UUID agentId, final UUID ruleId) {
        final AgentRule response = this.rejectAgentRule.execute(agentId, ruleId);
        return ResponseEntity.ok(this.agentRuleApiMapper.asAgentRuleDto(response));
    }
}
