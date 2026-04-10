package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.UUID;

/**
 * Reads one automation agent.
 */
public interface GetAgent {

    /**
     * Finds agent by identifier.
     *
     * @param agentId unique agent identifier.
     * @return persisted agent.
     */
    Agent execute(UUID agentId);
}
