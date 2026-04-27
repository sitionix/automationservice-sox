package com.sitionix.atmssox.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record CapabilityExecutionCommand(
        Long userId,
        Long agentId,
        UUID conversationId,
        JsonNode arguments
) {
}
