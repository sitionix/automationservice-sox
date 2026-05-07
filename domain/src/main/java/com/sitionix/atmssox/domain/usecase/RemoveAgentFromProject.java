package com.sitionix.atmssox.domain.usecase;

import java.util.UUID;

/**
 * Removes existing membership from project.
 */
public interface RemoveAgentFromProject {

    /**
     * Soft-deletes one active membership.
     *
     * @param projectId project identifier.
     * @param agentId agent identifier.
     */
    void execute(UUID projectId, UUID agentId);
}
