package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectMember {

    UUID membershipId;

    UUID projectId;

    UUID agentId;

    AgentProjectMemberStatus status;

    Instant createdAt;

    Instant updatedAt;
}
