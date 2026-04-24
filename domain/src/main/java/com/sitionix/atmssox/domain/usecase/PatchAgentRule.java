package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import java.util.UUID;

/**
 * Use case for patching one automation agent rule.
 */
public interface PatchAgentRule {

    /**
     * Updates one active rule for one owned agent.
     *
     * @param agentId agent identifier.
     * @param ruleId rule identifier.
     * @param command patch payload.
     * @return updated rule.
     */
    AgentRule execute(UUID agentId, UUID ruleId, PatchAgentRuleCommand command);
}
