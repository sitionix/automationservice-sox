package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchema;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.CapabilityProperty;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityDomainCoverageTest {

    @Mock
    private CapabilityHandler<Object> getSiteOverviewCapabilityHandler;

    @AfterEach
    void tearDown() {
        CapabilityName.GET_SITE_OVERVIEW.setHandler(null);
        CapabilityName.GET_WORKSPACE_SITES.setHandler(null);
        verifyNoMoreInteractions(this.getSiteOverviewCapabilityHandler);
    }

    @Test
    void givenCapabilityWithoutHandler_whenDefinition_thenThrowIllegalStateException() {
        //given

        //when
        //then
        assertThatThrownBy(() -> CapabilityName.GET_SITE_OVERVIEW.definition())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No handler configured for capability: GET_SITE_OVERVIEW");
    }

    @Test
    void givenCapabilityWithoutHandler_whenExecute_thenThrowIllegalStateException() {
        //given
        final CapabilityExecutionCommand<Object> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("8b829cc4-0f93-4fd3-ab8f-f25f3deb5b44"),
                Map.of("siteId", "12")
        );

        //when
        //then
        assertThatThrownBy(() -> CapabilityName.GET_SITE_OVERVIEW.execute(givenCommand))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No handler configured for capability: GET_SITE_OVERVIEW");
    }

    @Test
    void givenCapabilityWithHandler_whenDefinitionAndExecute_thenDelegateToHandler() {
        //given
        final CapabilityDefinition givenDefinition = new CapabilityDefinition(
                CapabilityName.GET_SITE_OVERVIEW.name(),
                "desc",
                List.of("site"),
                new CapabilityInputSchema("object", Map.of(), List.of(), Boolean.FALSE),
                "output"
        );
        final JsonNode payload = new ObjectMapper().valueToTree(Map.of("ok", true));
        final CapabilityExecutionCommand<Object> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("4aa3fec9-1f58-43b8-aadb-7ab6469d63a4"),
                Map.of("siteId", "12")
        );
        final CapabilityExecutionResult givenResult = new CapabilityExecutionResult(CapabilityName.GET_SITE_OVERVIEW, payload);
        CapabilityName.GET_SITE_OVERVIEW.setHandler(this.getSiteOverviewCapabilityHandler);
        when(this.getSiteOverviewCapabilityHandler.definition()).thenReturn(givenDefinition);
        when(this.getSiteOverviewCapabilityHandler.execute(givenCommand)).thenReturn(givenResult);

        //when
        final CapabilityDefinition actualDefinition = CapabilityName.GET_SITE_OVERVIEW.definition();
        final CapabilityExecutionResult actualResult = CapabilityName.GET_SITE_OVERVIEW.execute(givenCommand);

        //then
        assertThat(actualDefinition).isEqualTo(givenDefinition);
        assertThat(actualResult).isEqualTo(givenResult);
        verify(this.getSiteOverviewCapabilityHandler).definition();
        verify(this.getSiteOverviewCapabilityHandler).execute(givenCommand);
    }

    @Test
    void givenSchemaBuilderWithoutAdditionalPropertiesCall_whenBuild_thenUseFalseByDefault() {
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
    void givenSchemaBuilderWithOverriddenValues_whenBuild_thenReturnConfiguredSchema() {
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
