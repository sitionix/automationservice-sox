package com.sitionix.atmssox.domain.model.capability;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityInputSchemaBuilderTest {

    @Test
    void givenBuilderWithoutAdditionalPropertiesCall_whenBuild_thenUseFalseByDefault() {
        //given
        final CapabilityInputSchemaBuilder givenBuilder = CapabilityInputSchemaBuilder.objectSchema()
                .property("siteId", "string", "uuid", "Site identifier")
                .required("siteId");

        //when
        final CapabilityInputSchema actual = givenBuilder.build();

        //then
        assertThat(actual.type()).isEqualTo("object");
        assertThat(actual.properties()).isEqualTo(
                Map.of("siteId", new CapabilityProperty("string", "uuid", "Site identifier"))
        );
        assertThat(actual.required()).isEqualTo(List.of("siteId"));
        assertThat(actual.additionalProperties()).isEqualTo(Boolean.FALSE);
    }

    @Test
    void givenBuilderWithOverriddenValues_whenBuild_thenReturnConfiguredSchema() {
        //given
        final CapabilityInputSchemaBuilder givenBuilder = CapabilityInputSchemaBuilder.objectSchema()
                .type("custom")
                .property("workspaceId", "string", null, "Workspace identifier")
                .additionalProperties(true);

        //when
        final CapabilityInputSchema actual = givenBuilder.build();

        //then
        assertThat(actual.type()).isEqualTo("custom");
        assertThat(actual.properties()).isEqualTo(
                Map.of("workspaceId", new CapabilityProperty("string", null, "Workspace identifier"))
        );
        assertThat(actual.required()).isEqualTo(List.of());
        assertThat(actual.additionalProperties()).isEqualTo(Boolean.TRUE);
    }
}
