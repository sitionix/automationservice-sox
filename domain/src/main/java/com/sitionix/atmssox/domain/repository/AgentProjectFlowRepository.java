package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for agent project flow read operations.
 */
public interface AgentProjectFlowRepository {

    /**
     * Finds persisted flow by project identifier.
     *
     * @param projectId project identifier.
     * @return flow when persisted for project.
     */
    Optional<AgentProjectFlow> findByProjectId(UUID projectId);
}
