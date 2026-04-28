package com.sitionix.atmssox.domain.model.capability;

import java.util.List;

public record CapabilityDefinition(
        String name,
        String description,
        List<String> tags,
        CapabilityInputSchema inputSchema,
        String outputDescription
) {
}
