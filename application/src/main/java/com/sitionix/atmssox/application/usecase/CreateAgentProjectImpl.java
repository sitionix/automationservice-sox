package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.TextNormalizer;
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
        final String normalizedName = TextNormalizer.normalizeRequired(
                command.name(),
                "Project name must not be blank",
                NAME_MAX_LENGTH,
                "Project name must be between 1 and 120 characters"
        );
        final String normalizedDescription = TextNormalizer.normalizeOptionalNullable(
                command.description(),
                DESCRIPTION_MAX_LENGTH,
                "Project description must be at most 1000 characters"
        );
        final Long ownerUserId = this.authenticatedUserProvider.getUserId();
        return this.agentProjectRepository.save(AgentProject.builder()
                .id(UUID.randomUUID())
                .ownerUserId(ownerUserId)
                .name(normalizedName)
                .description(normalizedDescription)
                .status(AgentProjectStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
