package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Agent {

    UUID id;

    Long userId;

    String name;

    String description;

    String instruction;

    AgentStatus status;

    Instant createdAt;

    Instant updatedAt;
}
