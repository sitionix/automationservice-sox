package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectAgent;
import java.util.UUID;

/**
 * Adds existing agent to project membership.
 */
public interface AddAgentToProject {

    /**
     * Adds one agent to one project using idempotent semantics.
     *
     * @param projectId project identifier.
     * @param agentId agent identifier.
     * @return attached project agent.
     */
    ProjectAgent execute(UUID projectId, UUID agentId);
}
