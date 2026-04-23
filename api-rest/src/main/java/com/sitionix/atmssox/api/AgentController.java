package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentApi;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
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
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
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
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentController implements AgentApi {

    private final CreateAgent createAgent;

    private final GetAgents getAgents;

    private final GetAgent getAgent;

    private final GetAgentConversations getAgentConversations;

    private final GetAgentConversation getAgentConversation;

    private final PatchAgent patchAgent;

    private final ActivateAgent activateAgent;

    private final ArchiveAgent archiveAgent;

    private final ChatAgent chatAgent;

    private final CreateAgentRule createAgentRule;

    private final GetAgentRules getAgentRules;

    private final PatchAgentRule patchAgentRule;

    private final DeleteAgentRule deleteAgentRule;

    private final RestoreAgent restoreAgent;

    private final DeleteAgent deleteAgent;

    private final AgentApiMapper agentApiMapper;

    @Override
    public ResponseEntity<AgentDTO> createAgent(@Valid final CreateAgentRequestDTO createAgentRequestDTO) {
        final CreateAgentCommand command = this.agentApiMapper.asCreateAgentCommand(createAgentRequestDTO);
        final Agent agent = this.createAgent.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.agentApiMapper.asAgentDto(agent));
    }

    @Override
    public ResponseEntity<AgentsResponseDTO> getAgents() {
        return ResponseEntity.ok(this.agentApiMapper.asAgentsResponseDto(this.getAgents.execute()));
    }

    @Override
    public ResponseEntity<AgentDTO> getAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.getAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentConversationsResponseDTO> getAgentConversations(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentConversationsResponseDto(this.getAgentConversations.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentConversationDetailsDTO> getAgentConversation(final UUID conversationId) {
        final ConversationDetails details = this.getAgentConversation.execute(conversationId);
        return ResponseEntity.ok(this.agentApiMapper.asAgentConversationDetailsDto(details));
    }

    @Override
    public ResponseEntity<AgentDTO> patchAgent(final UUID agentId, @Valid final PatchAgentRequestDTO patchAgentRequestDTO) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(
                this.patchAgent.execute(agentId, this.agentApiMapper.asPatchAgentCommand(patchAgentRequestDTO))
        ));
    }

    @Override
    public ResponseEntity<AgentDTO> activateAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.activateAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentDTO> archiveAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.archiveAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<ChatAgentResponseDTO> chatAgent(final UUID agentId, @Valid final ChatAgentRequestDTO chatAgentRequestDTO) {
        return ResponseEntity.ok(this.agentApiMapper.asChatAgentResponseDto(
                this.chatAgent.execute(agentId, this.agentApiMapper.asChatAgentCommand(chatAgentRequestDTO))
        ));
    }

    @Override
    public ResponseEntity<AgentRulesResponseDTO> getAgentRules(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentRulesResponseDto(this.getAgentRules.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> createAgentRule(final UUID agentId, @Valid final CreateAgentRuleRequestDTO createAgentRuleRequestDTO) {
        final CreateAgentRuleCommand command = this.agentApiMapper.asCreateAgentRuleCommand(createAgentRuleRequestDTO);
        final AgentRule response = this.createAgentRule.execute(agentId, command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.agentApiMapper.asAgentRuleDto(response));
    }

    @Override
    public ResponseEntity<AgentRuleDTO> patchAgentRule(final UUID agentId,
                                                       final UUID ruleId,
                                                       @Valid final PatchAgentRuleRequestDTO patchAgentRuleRequestDTO) {
        final AgentRule response = this.patchAgentRule.execute(agentId, ruleId, this.agentApiMapper.asPatchAgentRuleCommand(patchAgentRuleRequestDTO));
        return ResponseEntity.ok(this.agentApiMapper.asAgentRuleDto(response));
    }

    @Override
    public ResponseEntity<DeleteAgentRuleResponseDTO> deleteAgentRule(final UUID agentId, final UUID ruleId) {
        return ResponseEntity.ok(this.agentApiMapper.asDeleteAgentRuleResponseDto(this.deleteAgentRule.execute(agentId, ruleId)));
    }

    @Override
    public ResponseEntity<AgentDTO> restoreAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.restoreAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentDTO> deleteAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.deleteAgent.execute(agentId)));
    }
}
