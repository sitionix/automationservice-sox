package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.CreateAgent;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAgentImpl implements CreateAgent {

    private static final int NAME_MAX_LENGTH = 60;
    private static final int DESCRIPTION_MAX_LENGTH = 160;

    private final AgentRepository agentRepository;

    @Override
    @Transactional
    public Agent execute(final CreateAgentCommand command) {
        final Instant now = Instant.now();
        return this.agentRepository.save(Agent.builder()
                .id(UUID.randomUUID())
                .name(this.normalizeRequired(command.name(), "Agent name is required", NAME_MAX_LENGTH, "Agent name must be between 1 and 60 characters"))
                .description(this.normalizeRequired(
                        command.description(),
                        "Agent description is required",
                        DESCRIPTION_MAX_LENGTH,
                        "Agent description must be between 1 and 160 characters"
                ))
                .status(AgentStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private String normalizeRequired(final String value,
                                     final String missingMessage,
                                     final int maxLength,
                                     final String lengthMessage) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException(missingMessage);
        }
        if (normalized.length() > maxLength) {
            throw new AgentValidationException(lengthMessage);
        }
        return normalized;
    }
}
