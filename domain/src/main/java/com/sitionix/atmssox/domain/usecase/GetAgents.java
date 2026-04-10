package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import java.util.List;

/**
 * Reads automation agents for UI rendering.
 */
public interface GetAgents {

    /**
     * Returns persisted agents.
     *
     * @return agent collection.
     */
    List<Agent> execute();
}
