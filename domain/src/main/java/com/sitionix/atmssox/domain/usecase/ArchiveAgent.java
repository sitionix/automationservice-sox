package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.UUID;

/**
 * Archives one automation agent.
 */
public interface ArchiveAgent {

    /**
     * Applies lifecycle transition to ARCHIVED for one agent.
     *
     * @param agentId unique agent identifier.
     * @return updated persisted agent.
     */
    Agent execute(UUID agentId);
}
