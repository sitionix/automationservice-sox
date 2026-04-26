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
    List<Agent> findAllVisibleByUserId(Long userId);

    /**
     * Finds visible (non-deleted) agent by identifier.
     *
     * @param agentId unique agent identifier.
     * @return agent when present.
     */
    Optional<Agent> findVisibleByIdAndUserId(UUID agentId, Long userId);

    /**
     * Finds agent by identifier including deleted state.
     *
     * @param agentId unique agent identifier.
     * @return agent when present.
     */
    Optional<Agent> findByIdAndUserId(UUID agentId, Long userId);

    /**
     * Finds one agent by identifier without user-scope restriction.
     *
     * @param agentId unique agent identifier.
     * @return agent when present.
     */
    Optional<Agent> findById(UUID agentId);

    /**
     * Finds internal system agent used for rule analysis.
     *
     * @return analyzer agent when present.
     */
    Optional<Agent> findSystemRuleAnalyzer();

    /**
     * Finds internal system agent used for context optimization.
     *
     * @return context optimizer agent when present.
     */
    Optional<Agent> findSystemContextOptimizer();
}
