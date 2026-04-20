package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAgentImpl implements GetAgent {

    private final AgentRepository agentRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public Agent execute(final UUID agentId) {
        return this.agentRepository.findVisibleByIdAndUserId(agentId, this.authenticatedUserProvider.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));
    }
}
