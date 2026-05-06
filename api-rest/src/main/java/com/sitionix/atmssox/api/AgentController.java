package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentApi;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
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
    private final PatchAgent patchAgent;
    private final ActivateAgent activateAgent;
    private final ArchiveAgent archiveAgent;
    private final RestoreAgent restoreAgent;
    private final DeleteAgent deleteAgent;
    private final AgentApiMapper agentApiMapper;

    @Override
    public ResponseEntity<AgentDTO> createAgent(@Valid final CreateAgentRequestDTO createAgentRequestDTO) {
        final CreateAgentCommand command = this.agentApiMapper.asCreateAgentCommand(createAgentRequestDTO);
        final Agent agent = this.createAgent.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.agentApiMapper.asAgentDto(agent));
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
    public ResponseEntity<AgentDTO> restoreAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.restoreAgent.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentDTO> deleteAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.deleteAgent.execute(agentId)));
    }
}
