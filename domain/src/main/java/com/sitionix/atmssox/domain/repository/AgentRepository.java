package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for automation agent aggregate operations.
 */
public interface AgentRepository {

    /**
     * Stores an agent.
     *
     * @param agent agent to persist.
     * @return persisted state.
     */
    Agent save(Agent agent);

    /**
     * Returns all persisted agents ordered for UI rendering.
     *
     * @return persisted agents.
     */
    List<Agent> findAll();

    /**
     * Finds agent by identifier.
     *
     * @param agentId unique agent identifier.
     * @return agent when present.
     */
    Optional<Agent> findById(UUID agentId);
}
