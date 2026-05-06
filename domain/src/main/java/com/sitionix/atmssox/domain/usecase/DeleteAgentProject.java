package com.sitionix.atmssox.domain.usecase;

import java.util.UUID;

/**
 * Use case for soft deleting one automation project.
 */
public interface DeleteAgentProject {

    /**
     * Soft deletes one automation project.
     *
     * @param projectId project identifier.
     */
    void execute(UUID projectId);
}
