package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Agent {

    UUID id;

    String name;

    String description;

    AgentStatus status;

    Instant createdAt;

    Instant updatedAt;
}
