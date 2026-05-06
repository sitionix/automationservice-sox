package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProject;
import java.util.UUID;

/**
 * Returns one automation project for current user.
 */
public interface GetAgentProject {

    /**
     * Returns one visible (non-deleted) project by id.
     *
     * @param projectId project identifier.
     * @return project details.
     */
    AgentProject execute(UUID projectId);
}
