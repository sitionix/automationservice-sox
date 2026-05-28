package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import java.util.UUID;

/**
 * Returns flow palette for one project.
 */
public interface GetAgentProjectFlowPalette {

    /**
     * Returns flow palette sources for current user-visible project.
     *
     * @param projectId project identifier.
     * @return flow palette response.
     */
    AgentProjectFlowPalette execute(UUID projectId);
}
