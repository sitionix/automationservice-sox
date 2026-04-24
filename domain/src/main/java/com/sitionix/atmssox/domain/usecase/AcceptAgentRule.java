package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRule;
import java.util.UUID;

/**
 * Use case for accepting one automation agent rule.
 */
public interface AcceptAgentRule {

    /**
     * Accepts one owned rule.
     *
     * @param agentId agent identifier.
     * @param ruleId rule identifier.
     * @param command optional title/content update payload.
     * @return updated rule.
     */
    AgentRule execute(UUID agentId, UUID ruleId, AcceptAgentRuleCommand command);
}
