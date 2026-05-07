package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectAgent;
import java.util.List;
import java.util.UUID;

/**
 * Returns attached project agents.
 */
public interface GetAgentProjectAgents {

    /**
     * Returns visible active members for one project.
     *
     * @param projectId project identifier.
     * @return attached project agents.
     */
    List<ProjectAgent> execute(UUID projectId);
}
