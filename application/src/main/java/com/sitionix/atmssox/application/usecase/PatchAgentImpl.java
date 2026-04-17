package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.PatchAgent;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatchAgentImpl implements PatchAgent {

    private static final int NAME_MAX_LENGTH = 60;
    private static final int DESCRIPTION_MAX_LENGTH = 160;

    private final AgentRepository agentRepository;
    private final ForgeUserClient forgeUserClient;

    @Override
    @Transactional
    public Agent execute(final UUID agentId, final PatchAgentCommand command) {
        final Agent current = this.agentRepository.findByIdAndUserId(agentId, this.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        final boolean hasName = command.name() != null;
        final boolean hasDescription = command.description() != null;
        final boolean hasInstruction = command.instruction() != null;
        if (!hasName && !hasDescription && !hasInstruction) {
            throw new AgentValidationException("At least one field (name, description or instruction) must be provided");
        }

        final String updatedName = hasName
                ? this.normalizeName(command.name())
                : current.getName();
        final String updatedDescription = hasDescription
                ? this.normalizeProvided(
                command.description(),
                DESCRIPTION_MAX_LENGTH,
                "Agent description must be between 1 and 160 characters"
        )
                : current.getDescription();
        final String updatedInstruction = hasInstruction
                ? this.normalizeTextProvided(
                command.instruction(),
                "Agent instruction must not be blank"
        )
                : current.getInstruction();

        return this.agentRepository.save(Agent.builder()
                .id(current.getId())
                .userId(current.getUserId())
                .name(updatedName)
                .description(updatedDescription)
                .instruction(updatedInstruction)
                .status(current.getStatus())
                .createdAt(current.getCreatedAt())
                .updatedAt(Instant.now())
                .build());
    }

    private String normalizeProvided(final String value,
                                     final int maxLength,
                                     final String lengthMessage) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new AgentValidationException(lengthMessage);
        }
        return normalized;
    }

    private String normalizeTextProvided(final String value,
                                         final String missingMessage) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new AgentValidationException(missingMessage);
        }
        return normalized;
    }

    private String normalizeName(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Agent name is required");
        }
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new AgentValidationException("Agent name must be between 1 and 60 characters");
        }
        return normalized;
    }

    private Long getUserId() {
        try {
            return this.forgeUserClient.getUserId();
        } catch (final RuntimeException exception) {
            throw new AuthenticationRequiredException("Authentication required");
        }
    }
}
