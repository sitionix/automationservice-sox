package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import java.util.Optional;
import java.util.UUID;

/**
 * Read persistence contract for project flow graph.
 */
public interface AgentProjectFlowRepository {

    /**
     * Returns one flow graph by project id.
     *
     * @param projectId project identifier.
     * @return flow graph when exists.
     */
    Optional<AgentProjectFlow> findByProjectId(UUID projectId);
}
