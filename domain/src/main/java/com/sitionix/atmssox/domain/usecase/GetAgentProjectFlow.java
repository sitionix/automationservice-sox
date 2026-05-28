package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import java.util.UUID;

/**
 * Returns one project flow graph.
 */
public interface GetAgentProjectFlow {

    /**
     * Returns project flow graph for current user-visible project.
     *
     * @param projectId project identifier.
     * @return flow graph response.
     */
    AgentProjectFlow execute(UUID projectId);
}
