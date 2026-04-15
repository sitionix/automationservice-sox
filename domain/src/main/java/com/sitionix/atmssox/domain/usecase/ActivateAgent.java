package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.UUID;

/**
 * Activates one automation agent.
 */
public interface ActivateAgent {

    /**
     * Applies lifecycle transition to ACTIVE for one agent.
     *
     * @param agentId unique agent identifier.
     * @return updated persisted agent.
     */
    Agent execute(UUID agentId);
}
