package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.ArchiveAgent;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArchiveAgentImpl implements ArchiveAgent {

    private final AgentRepository agentRepository;
    private final ForgeUserClient forgeUserClient;

    @Override
    @Transactional
    public Agent execute(final UUID agentId) {
        final Agent current = this.agentRepository.findByIdAndUserId(agentId, this.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        return this.agentRepository.save(current.toBuilder()
                .status(current.getStatus().archive())
                .updatedAt(Instant.now())
                .build());
    }

    private Long getUserId() {
        try {
            return this.forgeUserClient.getUserId();
        } catch (final RuntimeException exception) {
            throw new AuthenticationRequiredException("Authentication required");
        }
    }
}
