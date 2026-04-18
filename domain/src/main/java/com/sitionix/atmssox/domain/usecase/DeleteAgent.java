package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.UUID;

/**
 * Soft deletes one automation agent.
 */
public interface DeleteAgent {

    /**
     * Applies lifecycle transition to DELETED for one agent.
     *
     * @param agentId unique agent identifier.
     * @return updated persisted agent.
     */
    Agent execute(UUID agentId);
}
