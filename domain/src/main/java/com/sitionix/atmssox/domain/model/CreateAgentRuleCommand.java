package com.sitionix.atmssox.domain.model;

public record CreateAgentRuleCommand(
        String title,
        String content,
        AgentRuleStatus status,
        AgentRuleAuthorType authorType
) {
}
