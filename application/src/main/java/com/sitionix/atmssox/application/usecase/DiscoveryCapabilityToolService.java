package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolDefinition;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscoveryCapabilityToolService {

    public static final String TOOL_NAME = "DISCOVER_CAPABILITIES";

    private final CapabilityToolPayloadCodec payloadCodec;
    private final ObjectMapper objectMapper;

    public OpenAiNativeToolDefinition getDefinition() {
        return new OpenAiNativeToolDefinition(
                TOOL_NAME,
                "Discover backend capabilities relevant to the user's current intent.",
                this.objectMapper.valueToTree(
                        CapabilityInputSchemaBuilder.objectSchema()
                                .property("userIntent", "string", null, "Short description of what the user wants to accomplish.")
                                .required("userIntent")
                                .additionalProperties(false)
                                .build()
                ),
                true
        );
    }

    public boolean isDiscoveryCall(final OpenAiNativeToolCall toolCall) {
        return TOOL_NAME.equals(toolCall.name());
    }

    public String extractUserIntent(final OpenAiNativeToolCall toolCall) {
        final String userIntent;
        try {
            final DiscoveryRequest request = this.objectMapper.treeToValue(
                    this.payloadCodec.parseArgs(toolCall.argumentsJson()),
                    DiscoveryRequest.class
            );
            userIntent = request == null ? "" : request.userIntent();
        } catch (Exception exception) {
            return "User asks for assistance";
        }
        if (userIntent == null) {
            return "User asks for assistance";
        }
        final String normalizedUserIntent = userIntent.trim();
        if (normalizedUserIntent.isEmpty()) {
            return "User asks for assistance";
        }
        return normalizedUserIntent;
    }

    public OpenAiNativeToolResult buildDiscoveryResult(final OpenAiNativeToolCall toolCall,
                                                       final List<CapabilityDefinition> discoveredCapabilities) {
        return new OpenAiNativeToolResult(
                toolCall.callId(),
                this.payloadCodec.serializeDiscoveredCapabilities(discoveredCapabilities)
        );
    }

    private record DiscoveryRequest(String userIntent) {
    }
}
