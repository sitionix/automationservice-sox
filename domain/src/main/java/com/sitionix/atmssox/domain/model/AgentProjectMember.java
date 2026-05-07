package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectMember {

    private UUID membershipId;

    private UUID projectId;

    private UUID agentId;

    private AgentProjectMemberStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
