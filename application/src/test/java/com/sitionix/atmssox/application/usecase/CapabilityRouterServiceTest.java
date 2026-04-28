package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityRouterServiceTest {

    private CapabilityRouterService capabilityRouterService;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @Mock
    private CapabilityHandler<Object> getWorkspaceSitesCapabilityHandler;

    @Mock
    private CapabilityHandler<Object> getSiteOverviewCapabilityHandler;

    private AutomationCapabilitiesProperties automationCapabilitiesProperties;

    @BeforeEach
    void setUp() {
        this.automationCapabilitiesProperties = new AutomationCapabilitiesProperties();
        this.automationCapabilitiesProperties.getDiscovery().setMaxSelectedCapabilities(2);
        this.capabilityRouterService = new CapabilityRouterService(
                this.openAiChatClient,
                this.automationCapabilitiesProperties,
                new ObjectMapper()
        );
        when(this.getWorkspaceSitesCapabilityHandler.definition()).thenReturn(this.getDefinition("GET_WORKSPACE_SITES"));
        when(this.getSiteOverviewCapabilityHandler.definition()).thenReturn(this.getDefinition("GET_SITE_OVERVIEW"));
        CapabilityName.GET_WORKSPACE_SITES.setHandler(this.getWorkspaceSitesCapabilityHandler);
        CapabilityName.GET_SITE_OVERVIEW.setHandler(this.getSiteOverviewCapabilityHandler);
    }

    @AfterEach
    void tearDown() {
        CapabilityName.GET_WORKSPACE_SITES.setHandler(null);
        CapabilityName.GET_SITE_OVERVIEW.setHandler(null);
    }

    @Test
    void givenRouterResponseWithUnknownAndDuplicates_whenDiscover_thenFilterAndLimitCapabilities() {
        //given
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class))).thenReturn(
                "{\"capabilities\":[\"GET_WORKSPACE_SITES\",\"UNKNOWN\",\"GET_WORKSPACE_SITES\",\"GET_SITE_OVERVIEW\"]}"
        );

        //when
        final List<CapabilityDefinition> actual = this.capabilityRouterService.discover("analyze sites");

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual.stream().map(CapabilityDefinition::name).toList())
                .containsExactlyInAnyOrder("GET_WORKSPACE_SITES", "GET_SITE_OVERVIEW");
    }

    @Test
    void givenInvalidRouterJson_whenDiscover_thenReturnEmptyList() {
        //given
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class))).thenReturn("not-json");

        //when
        final List<CapabilityDefinition> actual = this.capabilityRouterService.discover("analyze sites");

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void givenRouterFailure_whenDiscover_thenReturnEmptyList() {
        //given
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class))).thenThrow(new RuntimeException("boom"));

        //when
        final List<CapabilityDefinition> actual = this.capabilityRouterService.discover("analyze sites");

        //then
        assertThat(actual).isEmpty();
    }

    private CapabilityDefinition getDefinition(final String name) {
        return new CapabilityDefinition(
                name,
                "description",
                List.of("tag"),
                CapabilityInputSchemaBuilder.objectSchema().build(),
                "out"
        );
    }
}
