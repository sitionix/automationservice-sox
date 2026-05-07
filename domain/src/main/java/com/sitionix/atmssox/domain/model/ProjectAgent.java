package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProjectAgent {

    private UUID id;

    private String name;

    private String description;

    private AgentStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID membershipId;

    private Instant attachedAt;
}
