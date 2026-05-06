package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;

/**
 * Persistence contract for automation project aggregate operations.
 */
public interface AgentProjectRepository {

    /**
     * Stores an agent project.
     *
     * @param project project to persist.
     * @return persisted state.
     */
    AgentProject save(AgentProject project);

    /**
     * Returns visible (non-deleted) projects for one owner.
     *
     * @param ownerUserId owner identifier.
     * @param page zero-based page number.
     * @param size requested page size.
     * @return one page of visible projects.
     */
    AgentProjectsPage findAllVisibleByOwnerUserId(Long ownerUserId, int page, int size);
}
