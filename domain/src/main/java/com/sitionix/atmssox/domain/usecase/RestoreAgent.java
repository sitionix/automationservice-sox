package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.UUID;

/**
 * Restores one archived automation agent.
 */
public interface RestoreAgent {

    /**
     * Applies lifecycle transition to DRAFT for one archived agent.
     *
     * @param agentId unique agent identifier.
     * @return updated persisted agent.
     */
    Agent execute(UUID agentId);
}
