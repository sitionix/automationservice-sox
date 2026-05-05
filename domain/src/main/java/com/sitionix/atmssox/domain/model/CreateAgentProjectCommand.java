package com.sitionix.atmssox.domain.model;

import lombok.Builder;

@Builder
public record CreateAgentProjectCommand(
        String name,
        String description
) {
}
