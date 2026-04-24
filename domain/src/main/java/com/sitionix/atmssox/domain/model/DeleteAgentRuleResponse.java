package com.sitionix.atmssox.domain.model;

import lombok.Builder;

@Builder
public record DeleteAgentRuleResponse(
        AgentRuleStatus status
) {
}
