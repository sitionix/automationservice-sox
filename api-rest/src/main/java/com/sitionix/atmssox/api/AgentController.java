package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentApi;
import com.app_afesox.atmssox.api_first.dto.Agent;
import com.app_afesox.atmssox.api_first.dto.AgentsResponse;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequest;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.atmssox.domain.usecase.GetAgents;
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

    private final AgentApiMapper agentApiMapper;

    @Override
    public ResponseEntity<Agent> createAgent(@Valid final CreateAgentRequest createAgentRequest) {
        final CreateAgentCommand command = this.agentApiMapper.asCreateAgentCommand(createAgentRequest);
        final com.sitionix.atmssox.domain.model.Agent agent = this.createAgent.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.agentApiMapper.asAgentDto(agent));
    }

    @Override
    public ResponseEntity<AgentsResponse> getAgents() {
        return ResponseEntity.ok(this.agentApiMapper.asAgentsResponseDto(this.getAgents.execute()));
    }

    @Override
    public ResponseEntity<Agent> getAgent(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentDto(this.getAgent.execute(agentId)));
    }
}
