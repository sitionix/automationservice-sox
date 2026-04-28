package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscoveryCapabilityToolService {

    public static final String TOOL_NAME = "DISCOVER_CAPABILITIES";

    private final CapabilityToolPayloadCodec payloadCodec;
    private final ObjectMapper objectMapper;

    public CapabilityDefinition getDefinition() {
        final CapabilityDefinition definition = new CapabilityDefinition(
                TOOL_NAME,
                """
                Discover backend platform capabilities relevant to the current user request.

                Use this first when the user asks about platform data, workspace state, sites, domains, analytics, services, account state, or anything that may require backend data or actions.

                The tool returns relevant capabilities that can be used in the next step.
                """.strip(),
                List.of("capability", "discovery", "tooling"),
                CapabilityInputSchemaBuilder.objectSchema()
                        .property("userIntent", "string", null, "Short description of what the user wants to accomplish.")
                        .required("userIntent")
                        .additionalProperties(false)
                        .build(),
                "List of concrete capability definitions relevant to the current user intent."
        );
        log.info("[CAPABILITY_DIAG] discover tool schema={}", definition.inputSchema());
        return definition;
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
