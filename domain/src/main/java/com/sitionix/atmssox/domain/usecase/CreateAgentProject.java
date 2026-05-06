package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;

/**
 * Creates a new automation project.
 */
public interface CreateAgentProject {

    /**
     * Creates a project from input command.
     *
     * @param command create payload.
     * @return persisted project.
     */
    AgentProject execute(CreateAgentProjectCommand command);
}
