package com.sitionix.atmssox.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

public record CapabilityExecutionResult(
        CapabilityName capabilityName,
        JsonNode payload
) {
}
