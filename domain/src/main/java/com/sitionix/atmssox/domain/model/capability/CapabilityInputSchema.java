package com.sitionix.atmssox.domain.model.capability;

import java.util.List;
import java.util.Map;

public record CapabilityInputSchema(
        String type,
        Map<String, CapabilityProperty> properties,
        List<String> required,
        Boolean additionalProperties
) {
}
