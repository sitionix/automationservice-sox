package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProject {

    UUID id;

    Long ownerUserId;

    String name;

    String description;

    AgentProjectStatus status;

    Instant createdAt;

    Instant updatedAt;
}
