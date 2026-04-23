package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import java.util.UUID;

/**
 * Use case for creating one automation agent rule.
 */
public interface CreateAgentRule {

    /**
     * Creates one active rule for one owned agent.
     *
     * @param agentId agent identifier.
     * @param command create payload.
     * @return created rule.
     */
    AgentRule execute(UUID agentId, CreateAgentRuleCommand command);
}
