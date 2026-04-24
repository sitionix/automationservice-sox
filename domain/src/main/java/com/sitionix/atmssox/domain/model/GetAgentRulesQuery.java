package com.sitionix.atmssox.domain.model;

public record GetAgentRulesQuery(
        AgentRuleStatus status,
        AgentRuleAuthorType authorType
) {
}
