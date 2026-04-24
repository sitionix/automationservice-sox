package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import java.util.UUID;

/**
 * Use case for soft deleting one automation agent rule.
 */
public interface DeleteAgentRule {

    /**
     * Soft deletes one rule for one owned agent.
     *
     * @param agentId agent identifier.
     * @param ruleId rule identifier.
     * @return delete result status.
     */
    DeleteAgentRuleResponse execute(UUID agentId, UUID ruleId);
}
