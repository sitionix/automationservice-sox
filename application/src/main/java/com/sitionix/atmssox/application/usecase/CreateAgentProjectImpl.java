package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAgentProjectImpl implements CreateAgentProject {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;

    private final AgentProjectRepository agentProjectRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentProject execute(final CreateAgentProjectCommand command) {
        final Instant now = Instant.now();
        return this.agentProjectRepository.save(AgentProject.builder()
                .id(UUID.randomUUID())
                .ownerUserId(this.authenticatedUserProvider.getUserId())
                .name(this.normalizeRequired(command.name()))
                .description(this.normalizeOptional(command.description()))
                .status(AgentProjectStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private String normalizeRequired(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Project name must not be blank");
        }
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new AgentValidationException("Project name must be between 1 and 120 characters");
        }
        return normalized;
    }

    private String normalizeOptional(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > DESCRIPTION_MAX_LENGTH) {
            throw new AgentValidationException("Project description must be at most 1000 characters");
        }
        return normalized;
    }
}
