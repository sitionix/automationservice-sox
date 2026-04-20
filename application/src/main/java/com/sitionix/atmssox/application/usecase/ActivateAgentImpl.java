package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.ActivateAgent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateAgentImpl implements ActivateAgent {

    private final AgentRepository agentRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public Agent execute(final UUID agentId) {
        final Agent current = this.agentRepository.findByIdAndUserId(agentId, this.authenticatedUserProvider.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        return this.agentRepository.save(current.toBuilder()
                .status(current.getStatus().activate())
                .updatedAt(Instant.now())
                .build());
    }
}
