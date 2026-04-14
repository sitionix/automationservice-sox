package com.sitionix.atmssox.domain.model;

import lombok.Builder;

@Builder
public record PatchAgentCommand(
        String name,
        String description
) {
}
