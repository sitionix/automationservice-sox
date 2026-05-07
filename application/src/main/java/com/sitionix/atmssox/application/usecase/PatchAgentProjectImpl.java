package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
import com.sitionix.atmssox.domain.model.TextNormalizer;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.PatchAgentProject;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatchAgentProjectImpl implements PatchAgentProject {

    private static final int NAME_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private static final int CONTEXT_MAX_LENGTH = 5000;

    private final AgentProjectRepository agentProjectRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentProject execute(final UUID projectId, final PatchAgentProjectCommand command) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final AgentProject current = this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final boolean hasName = command.name() != null;
        final boolean hasDescription = command.description() != null;
        final boolean hasContext = command.context() != null;
        if (!hasName && !hasDescription && !hasContext) {
            throw new AgentValidationException("At least one field (name, description or context) must be provided");
        }

        final String updatedName = hasName
                ? TextNormalizer.normalizeRequired(
                command.name(),
                "Project name must not be blank",
                NAME_MAX_LENGTH,
                "Project name must be between 1 and 120 characters"
        )
                : current.getName();
        final String updatedDescription = hasDescription
                ? TextNormalizer.normalizeOptionalNullable(
                command.description(),
                DESCRIPTION_MAX_LENGTH,
                "Project description must be at most 1000 characters"
        )
                : current.getDescription();
        final String updatedContext = hasContext
                ? TextNormalizer.normalizeOptionalNullable(
                command.context(),
                CONTEXT_MAX_LENGTH,
                "Project context must be at most 5000 characters"
        )
                : current.getContext();

        return this.agentProjectRepository.save(current.toBuilder()
                .name(updatedName)
                .description(updatedDescription)
                .context(updatedContext)
                .updatedAt(Instant.now())
                .build());
    }
}
