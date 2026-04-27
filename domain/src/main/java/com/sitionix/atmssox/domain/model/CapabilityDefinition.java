package com.sitionix.atmssox.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record CapabilityDefinition(
        String name,
        String description,
        List<String> tags,
        JsonNode inputSchema,
        String outputDescription
) {
}
