package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAgentsImpl implements GetAgents {

    private final AgentRepository agentRepository;
    private final ForgeUserClient forgeUserClient;

    @Override
    public List<Agent> execute() {
        return this.agentRepository.findAllVisibleByUserId(this.getUserId());
    }

    private Long getUserId() {
        try {
            return this.forgeUserClient.getUserId();
        } catch (final RuntimeException exception) {
            throw new AuthenticationRequiredException("Authentication required");
        }
    }
}
