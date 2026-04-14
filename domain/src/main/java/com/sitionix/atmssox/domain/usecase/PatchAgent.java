package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import java.util.UUID;

/**
 * Partially updates one automation agent.
 */
public interface PatchAgent {

    /**
     * Applies partial identity update for one agent.
     *
     * @param agentId unique agent identifier.
     * @param command partial update payload.
     * @return updated persisted agent.
     */
    Agent execute(UUID agentId, PatchAgentCommand command);
}
