package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectRuntimeContextResolver {

    private final AgentProjectRepository agentProjectRepository;

    public Optional<ProjectRuntimeContext> resolve(final Long userId, final UUID projectId) {
        if (projectId == null) {
            return Optional.empty();
        }

        final AgentProject project = this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation project not found"));

        return Optional.of(new ProjectRuntimeContext(
                project.getId(),
                project.getName(),
                project.getContext()
        ));
    }
}
