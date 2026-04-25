package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentRule;
import java.util.UUID;

/**
 * Use case for rejecting one suggested automation rule.
 */
public interface RejectAgentRule {

    /**
     * Rejects one owned rule.
     *
     * @param agentId agent identifier.
     * @param ruleId rule identifier.
     * @return updated rule.
     */
    AgentRule execute(UUID agentId, UUID ruleId);
}
