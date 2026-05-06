package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;

/**
 * Returns automation projects for current user.
 */
public interface GetAgentProjects {

    /**
     * Returns one user-scoped page of projects.
     *
     * @param query pagination query.
     * @return paged project response.
     */
    AgentProjectsPage execute(GetAgentProjectsQuery query);
}
