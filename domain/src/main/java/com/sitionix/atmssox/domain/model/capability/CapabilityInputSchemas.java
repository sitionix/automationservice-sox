package com.sitionix.atmssox.domain.model.capability;

import java.util.List;
import java.util.Map;

/**
 * Reusable builders for capability input schemas.
 */
public final class CapabilityInputSchemas {

    private CapabilityInputSchemas() {
    }

    public static CapabilityInputSchema requiredSiteId() {
        return new CapabilityInputSchema(
                "object",
                Map.of("siteId", new CapabilityProperty("string", "uuid", "Site identifier")),
                List.of("siteId"),
                Boolean.FALSE
        );
    }

    public static CapabilityInputSchema emptyObject() {
        return new CapabilityInputSchema(
                "object",
                Map.of(),
                List.of(),
                Boolean.FALSE
        );
    }
}
