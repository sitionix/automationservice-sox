package com.sitionix.atmssox.domain.client;

import com.fasterxml.jackson.databind.JsonNode;

public record OpenAiNativeToolDefinition(
        String name,
        String description,
        JsonNode inputSchema,
        boolean strict
) {
}
