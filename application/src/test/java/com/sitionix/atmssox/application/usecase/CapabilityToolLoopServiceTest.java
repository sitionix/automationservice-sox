package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityToolLoopServiceTest {

    private CapabilityToolLoopService capabilityToolLoopService;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @Mock
    private CapabilityRouterService capabilityRouterService;

    @Mock
    private DiscoveryCapabilityToolService discoveryCapabilityToolService;

    @Mock
    private ConcreteCapabilityExecutionService concreteCapabilityExecutionService;

    private AutomationCapabilitiesProperties automationCapabilitiesProperties;

    @BeforeEach
    void setUp() {
        this.automationCapabilitiesProperties = new AutomationCapabilitiesProperties();
        this.automationCapabilitiesProperties.getExecution().setMaxDiscoveryCallsPerMessage(1);
        this.automationCapabilitiesProperties.getExecution().setMaxCapabilityCallsPerMessage(2);
        this.capabilityToolLoopService = new CapabilityToolLoopService(
                this.openAiChatClient,
                this.capabilityRouterService,
                this.discoveryCapabilityToolService,
                this.concreteCapabilityExecutionService,
                this.automationCapabilitiesProperties
        );
    }

    @Test
    void givenNoToolCalls_whenExecute_thenReturnOutputText() {
        //given
        when(this.discoveryCapabilityToolService.getDefinition()).thenReturn(this.getDefinition("DISCOVER_CAPABILITIES"));
        when(this.openAiChatClient.executeWithTools(any(OpenAiToolChatRequest.class)))
                .thenReturn(new OpenAiToolChatResponse("r1", "final", List.of()));

        //when
        final String actual = this.capabilityToolLoopService.execute(
                "inst",
                "input"
        );

        //then
        assertThat(actual).isEqualTo("final");
        verify(this.discoveryCapabilityToolService).getDefinition();
        verify(this.openAiChatClient).executeWithTools(any(OpenAiToolChatRequest.class));
    }

    @Test
    void givenCapabilitiesLoopRequest_whenExecute_thenInjectRuntimeCapabilityInstructionAndDiscoveryTool() {
        //given
        final CapabilityDefinition discoverDefinition = this.getDefinition("DISCOVER_CAPABILITIES");
        when(this.discoveryCapabilityToolService.getDefinition()).thenReturn(discoverDefinition);
        when(this.openAiChatClient.executeWithTools(any(OpenAiToolChatRequest.class)))
                .thenReturn(new OpenAiToolChatResponse("r1", "final", List.of()));
        final ArgumentCaptor<OpenAiToolChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiToolChatRequest.class);

        //when
        this.capabilityToolLoopService.execute(
                "Keep answers concise.",
                "які в мене є сайти?"
        );

        //then
        verify(this.openAiChatClient).executeWithTools(requestCaptor.capture());
        final OpenAiToolChatRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.tools()).extracting(CapabilityDefinition::name).containsExactly("DISCOVER_CAPABILITIES");
        assertThat(actualRequest.instruction()).contains("You can use backend platform capabilities through tools.");
        assertThat(actualRequest.instruction()).contains("use DISCOVER_CAPABILITIES before answering");
        assertThat(actualRequest.instruction()).contains("Do not say you lack access to platform data");
    }

    @Test
    void givenDiscoveryAndCapabilityCall_whenExecute_thenHandleBothAndReturnFinalOutput() {
        //given
        final CapabilityDefinition discoverDefinition = this.getDefinition("DISCOVER_CAPABILITIES");
        final CapabilityDefinition capabilityDefinition = this.getDefinition("GET_WORKSPACE_SITES");
        final OpenAiNativeToolCall discoveryCall = new OpenAiNativeToolCall("d1", "DISCOVER_CAPABILITIES", "{}" );
        final OpenAiNativeToolCall capabilityCall = new OpenAiNativeToolCall("c1", "GET_WORKSPACE_SITES", "{}" );

        when(this.discoveryCapabilityToolService.getDefinition()).thenReturn(discoverDefinition);
        when(this.discoveryCapabilityToolService.isDiscoveryCall(discoveryCall)).thenReturn(true);
        when(this.discoveryCapabilityToolService.extractUserIntent(discoveryCall)).thenReturn("intent");
        when(this.capabilityRouterService.discover("intent")).thenReturn(List.of(capabilityDefinition));
        when(this.discoveryCapabilityToolService.buildDiscoveryResult(discoveryCall, List.of(capabilityDefinition)))
                .thenReturn(new OpenAiNativeToolResult("d1", "{\"capabilities\":[]}"));
        when(this.discoveryCapabilityToolService.isDiscoveryCall(capabilityCall)).thenReturn(false);
        when(this.concreteCapabilityExecutionService.execute(capabilityCall))
                .thenReturn(new OpenAiNativeToolResult("c1", "{\"ok\":true}"));

        when(this.openAiChatClient.executeWithTools(any(OpenAiToolChatRequest.class)))
                .thenReturn(new OpenAiToolChatResponse("r1", null, List.of(discoveryCall)))
                .thenReturn(new OpenAiToolChatResponse("r2", null, List.of(capabilityCall)))
                .thenReturn(new OpenAiToolChatResponse("r3", "done", List.of()));

        //when
        final String actual = this.capabilityToolLoopService.execute(
                "inst",
                "input"
        );

        //then
        assertThat(actual).isEqualTo("done");
        verify(this.capabilityRouterService).discover("intent");
        verify(this.concreteCapabilityExecutionService).execute(capabilityCall);
    }

    private CapabilityDefinition getDefinition(final String name) {
        return new CapabilityDefinition(
                name,
                "d",
                List.of("t"),
                CapabilityInputSchemaBuilder.objectSchema().build(),
                "out"
        );
    }
}
