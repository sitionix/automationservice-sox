package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveUserAgentResolver {

    private final AgentRepository agentRepository;

    public Optional<Agent> findById(final UUID agentId) {
        return this.agentRepository.findById(agentId)
                .filter(agent -> agent.getType() == AgentType.USER)
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE);
    }
}
