package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryCapabilityToolServiceTest {

    private DiscoveryCapabilityToolService discoveryCapabilityToolService;

    @BeforeEach
    void setUp() {
        this.discoveryCapabilityToolService = new DiscoveryCapabilityToolService(
                new CapabilityToolPayloadCodec(new ObjectMapper()),
                new ObjectMapper()
        );
    }

    @Test
    void givenNoInput_whenGetDefinition_thenReturnDiscoveryCapabilityDefinition() {
        //given

        //when
        final CapabilityDefinition actual = this.discoveryCapabilityToolService.getDefinition();

        //then
        assertThat(actual.name()).isEqualTo(DiscoveryCapabilityToolService.TOOL_NAME);
        assertThat(actual.inputSchema().required()).containsExactly("userIntent");
    }

    @Test
    void givenDiscoveryToolCallWithBlankIntent_whenExtractUserIntent_thenReturnFallback() {
        //given
        final OpenAiNativeToolCall givenToolCall = new OpenAiNativeToolCall("c1", DiscoveryCapabilityToolService.TOOL_NAME, "{}");

        //when
        final String actual = this.discoveryCapabilityToolService.extractUserIntent(givenToolCall);

        //then
        assertThat(actual).isEqualTo("User asks for assistance");
    }

    @Test
    void givenDiscoveryToolCallAndCapabilities_whenBuildDiscoveryResult_thenReturnSerializedResult() {
        //given
        final OpenAiNativeToolCall givenToolCall = new OpenAiNativeToolCall("c1", DiscoveryCapabilityToolService.TOOL_NAME, "{}");
        final CapabilityDefinition givenCapability = this.discoveryCapabilityToolService.getDefinition();

        //when
        final OpenAiNativeToolResult actual = this.discoveryCapabilityToolService.buildDiscoveryResult(givenToolCall, List.of(givenCapability));

        //then
        assertThat(actual.callId()).isEqualTo("c1");
        assertThat(actual.outputJson()).contains("\"capabilities\"");
    }
}
