package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentRule;
import java.util.List;
import java.util.UUID;

/**
 * Use case for retrieving active automation agent rules.
 */
public interface GetAgentRules {

    /**
     * Returns active rules for one owned agent.
     *
     * @param agentId agent identifier.
     * @return active rules ordered by createdAt ascending.
     */
    List<AgentRule> execute(UUID agentId);
}
