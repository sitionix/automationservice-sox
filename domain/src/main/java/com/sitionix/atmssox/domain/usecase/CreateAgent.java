package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;

/**
 * Creates a new automation agent.
 */
public interface CreateAgent {

    /**
     * Creates an agent from input command.
     *
     * @param command create payload.
     * @return persisted agent.
     */
    Agent execute(CreateAgentCommand command);
}
