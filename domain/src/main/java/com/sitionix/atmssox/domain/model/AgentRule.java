package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentRule {

    UUID id;

    UUID agentId;

    String title;

    String content;

    AgentRuleStatus status;

    AgentRuleAuthorType authorType;

    Instant createdAt;

    Instant updatedAt;
}
