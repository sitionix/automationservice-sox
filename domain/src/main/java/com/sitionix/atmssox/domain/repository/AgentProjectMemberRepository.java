package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for project-agent membership operations.
 */
public interface AgentProjectMemberRepository {

    /**
     * Saves membership state.
     *
     * @param member membership state.
     * @return persisted state.
     */
    AgentProjectMember save(AgentProjectMember member);

    /**
     * Finds membership by project-agent pair.
     *
     * @param projectId project identifier.
     * @param agentId agent identifier.
     * @return membership when exists.
     */
    Optional<AgentProjectMember> findByProjectIdAndAgentId(UUID projectId, UUID agentId);

    /**
     * Finds active membership by project-agent pair.
     *
     * @param projectId project identifier.
     * @param agentId agent identifier.
     * @return active membership when exists.
     */
    Optional<AgentProjectMember> findActiveByProjectIdAndAgentId(UUID projectId, UUID agentId);

    /**
     * Returns attached agents visible for project owner.
     *
     * @param projectId project identifier.
     * @param ownerUserId authenticated owner.
     * @return visible attached agents.
     */
    List<ProjectAgent> findVisibleProjectAgents(UUID projectId, Long ownerUserId);
}
