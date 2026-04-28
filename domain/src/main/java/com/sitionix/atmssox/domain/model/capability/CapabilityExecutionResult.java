package com.sitionix.atmssox.domain.model.capability;

import com.fasterxml.jackson.databind.JsonNode;

public record CapabilityExecutionResult(
        CapabilityName capabilityName,
        JsonNode payload
) {
}
