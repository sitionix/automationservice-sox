package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentApi;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleAuthorTypeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleStatusDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentProjectApiMapper;
import com.sitionix.atmssox.api.mapper.AgentRuleApiMapper;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.atmssox.domain.usecase.AcceptAgentRule;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import com.sitionix.atmssox.domain.usecase.DeleteAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentChatExecution;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
import com.sitionix.atmssox.domain.usecase.GetAgentRules;
import com.sitionix.atmssox.domain.usecase.RejectAgentRule;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
import com.sitionix.atmssox.domain.usecase.PatchAgent;
import com.sitionix.atmssox.domain.usecase.RestoreAgent;
import com.sitionix.atmssox.domain.usecase.SubmitAgentChatExecution;
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
    private final GetAgentProjects getAgentProjects;

    private final GetAgent getAgent;

    private final GetAgentConversations getAgentConversations;

    private final GetAgentConversation getAgentConversation;

    private final PatchAgent patchAgent;

    private final ActivateAgent activateAgent;

    private final ArchiveAgent archiveAgent;

    private final SubmitAgentChatExecution submitAgentChatExecution;
    private final GetAgentChatExecution getAgentChatExecution;

    private final CreateAgentRule createAgentRule;
    private final CreateAgentProject createAgentProject;

    private final GetAgentRules getAgentRules;

    private final PatchAgentRule patchAgentRule;

    private final DeleteAgentRule deleteAgentRule;

    private final AcceptAgentRule acceptAgentRule;

    private final RejectAgentRule rejectAgentRule;

    private final RestoreAgent restoreAgent;

    private final DeleteAgent deleteAgent;
    private final DeleteAgentConversation deleteAgentConversation;

    private final AgentApiMapper agentApiMapper;
    private final AgentProjectApiMapper agentProjectApiMapper;

    private final AgentRuleApiMapper agentRuleApiMapper;

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
    public ResponseEntity<AgentProjectDTO> createAgentProject(@Valid final CreateAgentProjectRequestDTO createAgentProjectRequestDTO) {
        final CreateAgentProjectCommand command = this.agentApiMapper.asCreateAgentProjectCommand(createAgentProjectRequestDTO);
        final AgentProject response = this.createAgentProject.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.agentProjectApiMapper.asAgentProjectDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectsPageResponseDTO> getAgentProjects(final Integer page, final Integer size) {
        final AgentProjectsPage response = this.getAgentProjects.execute(GetAgentProjectsQuery.builder()
                .page(page)
                .size(size)
                .build());
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectsPageResponseDto(response));
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
    public ResponseEntity<SubmitChatExecutionResponseDTO> submitAgentChatExecution(final UUID agentId,
                                                                                    @Valid final ChatAgentRequestDTO chatAgentRequestDTO,
                                                                                    final String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.agentApiMapper.asSubmitChatExecutionResponseDto(
                this.submitAgentChatExecution.execute(agentId, this.agentApiMapper.asChatAgentCommand(chatAgentRequestDTO), idempotencyKey)
        ));
    }

    @Override
    public ResponseEntity<SubmitChatExecutionResponseDTO> submitAgentChatExecutionByExecutionsPath(final UUID agentId,
                                                                                                    @Valid final ChatAgentRequestDTO chatAgentRequestDTO,
                                                                                                    final String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.agentApiMapper.asSubmitChatExecutionResponseDto(
                this.submitAgentChatExecution.execute(agentId, this.agentApiMapper.asChatAgentCommand(chatAgentRequestDTO), idempotencyKey)
        ));
    }

    @Override
    public ResponseEntity<ChatExecutionDTO> getAgentChatExecution(final UUID agentId, final UUID executionId, final UUID conversationId) {
        return ResponseEntity.ok(this.agentApiMapper.asChatExecutionDto(
                this.getAgentChatExecution.execute(agentId, executionId, conversationId)
        ));
    }

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.agentRuleApiMapper.asAgentRuleDto(response));
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

    @Override
    public ResponseEntity<AgentDTO> restoreAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.restoreAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentDTO> deleteAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.deleteAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<Void> deleteAgentConversation(final UUID conversationId) {
        this.deleteAgentConversation.execute(conversationId);
        return ResponseEntity.noContent().build();
    }
}
