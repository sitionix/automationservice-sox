package com.sitionix.atmssox.domain.model;

import lombok.Builder;

@Builder
public record CreateAgentCommand(
        String name,
        String description
) {
}
