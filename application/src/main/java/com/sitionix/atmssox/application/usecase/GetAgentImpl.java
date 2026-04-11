package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.GetAgent;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAgentImpl implements GetAgent {

    private final AgentRepository agentRepository;
    private final ForgeUserClient forgeUserClient;

    @Override
    public Agent execute(final UUID agentId) {
        return this.agentRepository.findByIdAndUserId(agentId, this.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));
    }

    private Long getUserId() {
        try {
            return this.forgeUserClient.getUserId();
        } catch (final RuntimeException exception) {
            throw new AuthenticationRequiredException("Authentication required");
        }
    }
}
