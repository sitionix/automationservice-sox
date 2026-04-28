package com.sitionix.atmssox.domain.model.capability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CapabilityInputSchemaBuilder {

    private String type = "object";
    private final Map<String, CapabilityProperty> properties = new LinkedHashMap<>();
    private final List<String> required = new ArrayList<>();
    private Boolean additionalProperties = Boolean.FALSE;

    private CapabilityInputSchemaBuilder() {
    }

    public static CapabilityInputSchemaBuilder objectSchema() {
        return new CapabilityInputSchemaBuilder();
    }

    public CapabilityInputSchemaBuilder type(final String value) {
        this.type = value;
        return this;
    }

    public CapabilityInputSchemaBuilder property(
            final String name,
            final String type,
            final String format,
            final String description
    ) {
        this.properties.put(name, new CapabilityProperty(type, format, description));
        return this;
    }

    public CapabilityInputSchemaBuilder required(final String propertyName) {
        this.required.add(propertyName);
        return this;
    }

    public CapabilityInputSchemaBuilder additionalProperties(final boolean value) {
        this.additionalProperties = value;
        return this;
    }

    public CapabilityInputSchema build() {
        return new CapabilityInputSchema(
                this.type,
                Map.copyOf(this.properties),
                List.copyOf(this.required),
                this.additionalProperties
        );
    }
}
