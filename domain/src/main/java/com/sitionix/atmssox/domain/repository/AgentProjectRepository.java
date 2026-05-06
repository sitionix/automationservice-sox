package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Returns one visible (non-deleted) project by id for one owner.
     *
     * @param projectId project identifier.
     * @param ownerUserId owner identifier.
     * @return project when visible for current owner.
     */
    Optional<AgentProject> findVisibleByIdAndOwnerUserId(UUID projectId, Long ownerUserId);
}
