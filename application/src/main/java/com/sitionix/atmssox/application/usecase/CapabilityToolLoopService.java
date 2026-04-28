package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolDefinition;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.SiteOverviewArg;
import com.sitionix.atmssox.domain.model.capability.WorkspaceSitesArg;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapabilityToolLoopService {

    private static final String DISCOVER_CAPABILITIES = "DISCOVER_CAPABILITIES";

    private final OpenAiChatClient openAiChatClient;
    private final CapabilityRouterService capabilityRouterService;
    private final CapabilityToolDefinitionAdapter capabilityToolDefinitionAdapter;
    private final AutomationCapabilitiesProperties capabilitiesProperties;
    private final ObjectMapper objectMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public String execute(final String instruction, final String input) {
        final OpenAiNativeToolDefinition discoveryTool = new OpenAiNativeToolDefinition(
                DISCOVER_CAPABILITIES,
                "Discover backend capabilities relevant to the user's current intent.",
                this.objectMapper.valueToTree(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "userIntent", Map.of(
                                        "type", "string",
                                        "description", "Short description of what the user wants to accomplish."
                                )
                        ),
                        "required", List.of("userIntent"),
                        "additionalProperties", false
                )),
                true
        );

        final int maxDiscoveryCalls = this.capabilitiesProperties.getExecution().getMaxDiscoveryCallsPerMessage();
        final int maxCapabilityCalls = this.capabilitiesProperties.getExecution().getMaxCapabilityCallsPerMessage();

        int discoveryCalls = 0;
        int capabilityCalls = 0;
        String previousResponseId = null;
        List<OpenAiNativeToolDefinition> activeTools = List.of(discoveryTool);
        List<OpenAiNativeToolResult> toolResults = List.of();

        while (true) {
            final OpenAiToolChatResponse response = this.openAiChatClient.executeWithTools(
                    new OpenAiToolChatRequest(instruction, input, previousResponseId, activeTools, toolResults)
            );
            previousResponseId = response.responseId();
            if (response.toolCalls() == null || response.toolCalls().isEmpty()) {
                return response.outputText();
            }

            final List<OpenAiNativeToolResult> stepResults = new ArrayList<>();
            for (final OpenAiNativeToolCall toolCall : response.toolCalls()) {
                if (DISCOVER_CAPABILITIES.equals(toolCall.name())) {
                    if (discoveryCalls >= maxDiscoveryCalls) {
                        log.info("[CAPABILITY] execution limits reached discoveryCalls={}", discoveryCalls);
                        return response.outputText();
                    }
                    discoveryCalls++;
                    log.info("[CAPABILITY] discovery requested");
                    final String userIntent = this.extractUserIntent(toolCall.argumentsJson());
                    final List<CapabilityDefinition> selected = this.capabilityRouterService.discover(userIntent);
                    log.info("[CAPABILITY] router selected capabilities={}", selected.stream().map(CapabilityDefinition::name).toList());
                    activeTools = selected.stream().map(this.capabilityToolDefinitionAdapter::toNativeTool).toList();
                    stepResults.add(new OpenAiNativeToolResult(toolCall.callId(), this.serializeDiscoveredCapabilities(selected)));
                    continue;
                }

                if (capabilityCalls >= maxCapabilityCalls) {
                    log.info("[CAPABILITY] execution limits reached capabilityCalls={}", capabilityCalls);
                    return response.outputText();
                }
                capabilityCalls++;
                stepResults.add(this.executeCapability(toolCall));
            }
            toolResults = stepResults;
        }
    }

    private OpenAiNativeToolResult executeCapability(final OpenAiNativeToolCall toolCall) {
        try {
            final CapabilityName capabilityName = CapabilityName.valueOf(toolCall.name());
            log.info("[CAPABILITY] executing capability={}", capabilityName.name());
            final CapabilityExecutionResult result = switch (capabilityName) {
                case GET_WORKSPACE_SITES -> capabilityName.execute(new CapabilityExecutionCommand<>(
                        this.authenticatedUserProvider.getUserId(),
                        null,
                        UUID.randomUUID(),
                        this.objectMapper.treeToValue(this.parseArgs(toolCall.argumentsJson()), WorkspaceSitesArg.class)
                ));
                case GET_SITE_OVERVIEW -> capabilityName.execute(new CapabilityExecutionCommand<>(
                        this.authenticatedUserProvider.getUserId(),
                        null,
                        UUID.randomUUID(),
                        this.objectMapper.treeToValue(this.parseArgs(toolCall.argumentsJson()), SiteOverviewArg.class)
                ));
            };
            log.info("[CAPABILITY] capability executed capability={} success=true", capabilityName.name());
            return new OpenAiNativeToolResult(toolCall.callId(), this.nodeToString(result.payload()));
        } catch (Exception exception) {
            log.warn("[CAPABILITY] capability failed capability={} reason={}", toolCall.name(), exception.getMessage());
            return new OpenAiNativeToolResult(toolCall.callId(), this.serializeError("Capability call failed"));
        }
    }

    private String extractUserIntent(final String argumentsJson) {
        final JsonNode args = this.parseArgs(argumentsJson);
        final JsonNode userIntentNode = args.path("userIntent");
        if (!userIntentNode.isTextual() || userIntentNode.asText().trim().isEmpty()) {
            return "User asks for assistance";
        }
        return userIntentNode.asText();
    }

    private JsonNode parseArgs(final String argumentsJson) {
        try {
            return this.objectMapper.readTree(argumentsJson == null ? "{}" : argumentsJson);
        } catch (JsonProcessingException exception) {
            return this.objectMapper.createObjectNode();
        }
    }

    private String serializeDiscoveredCapabilities(final List<CapabilityDefinition> selected) {
        try {
            return this.objectMapper.writeValueAsString(Map.of("capabilities", selected));
        } catch (JsonProcessingException exception) {
            return "{\"capabilities\":[]}";
        }
    }

    private String serializeError(final String message) {
        try {
            return this.objectMapper.writeValueAsString(Map.of("error", message));
        } catch (JsonProcessingException exception) {
            return "{\"error\":\"internal_error\"}";
        }
    }

    private String nodeToString(final JsonNode payload) {
        try {
            return this.objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }
}
