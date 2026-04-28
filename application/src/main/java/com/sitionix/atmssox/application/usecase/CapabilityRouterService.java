package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapabilityRouterService {

    private static final String ROUTER_INSTRUCTION = """
            You select backend capabilities relevant to a user intent.

            Return only valid JSON:
            {
              \"capabilities\": [\"CAPABILITY_NAME\"]
            }

            Rules:
            - Select only capabilities that are useful for the intent.
            - Prefer fewer capabilities.
            - Do not invent capability names.
            - If none are useful, return {\"capabilities\":[]}.""";

    private final OpenAiChatClient openAiChatClient;
    private final OpenAiJsonResponseParser openAiJsonResponseParser;
    private final AutomationCapabilitiesProperties capabilitiesProperties;
    private final ObjectMapper objectMapper;

    public List<CapabilityDefinition> discover(final String userIntent) {
        final List<CapabilityDefinition> candidates = List.of(
                CapabilityName.GET_WORKSPACE_SITES.definition(),
                CapabilityName.GET_SITE_OVERVIEW.definition()
        );
        final List<Map<String, Object>> compactCatalog = candidates.stream().map(definition -> Map.<String, Object>of(
                "name", definition.name(),
                "description", definition.description(),
                "tags", definition.tags(),
                "outputDescription", definition.outputDescription()
        )).toList();
        final String input = this.serializeRouterInput(userIntent, compactCatalog);
        try {
            final String raw = this.openAiChatClient.execute(new OpenAiChatRequest(ROUTER_INSTRUCTION, input));
            return this.parseSelectedCapabilities(raw, candidates);
        } catch (RuntimeException exception) {
            log.warn("[CAPABILITY] capability router failed", exception);
            return List.of();
        }
    }

    private List<CapabilityDefinition> parseSelectedCapabilities(final String raw, final List<CapabilityDefinition> candidates) {
        final JsonNode root = this.openAiJsonResponseParser.parseObject(raw).orElse(null);
        if (root == null || !root.isObject() || !root.path("capabilities").isArray()) {
            log.warn("[CAPABILITY] invalid router response");
            return List.of();
        }
        final int max = this.capabilitiesProperties.getDiscovery().getMaxSelectedCapabilities();
        final Set<String> selected = new LinkedHashSet<>();
        for (final JsonNode capabilityNode : root.path("capabilities")) {
            if (!capabilityNode.isTextual()) {
                continue;
            }
            final String name = capabilityNode.asText();
            try {
                final CapabilityName capabilityName = CapabilityName.valueOf(name);
                selected.add(capabilityName.name());
            } catch (IllegalArgumentException exception) {
                log.warn("[CAPABILITY] router selected unknown capability={}", name);
            }
            if (selected.size() >= max) {
                break;
            }
        }
        return candidates.stream().filter(def -> selected.contains(def.name())).toList();
    }

    private String serializeRouterInput(final String userIntent, final List<Map<String, Object>> compactCatalog) {
        try {
            return this.objectMapper.writeValueAsString(Map.of(
                    "userIntent", userIntent,
                    "capabilities", compactCatalog
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize router input", exception);
        }
    }
}
