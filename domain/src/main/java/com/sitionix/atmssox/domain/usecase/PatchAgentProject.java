package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
import java.util.UUID;

/**
 * Use case for patching one automation project.
 */
public interface PatchAgentProject {

    /**
     * Applies partial update for one automation project.
     *
     * @param projectId project identifier.
     * @param command patch payload.
     * @return updated project.
     */
    AgentProject execute(UUID projectId, PatchAgentProjectCommand command);
}
