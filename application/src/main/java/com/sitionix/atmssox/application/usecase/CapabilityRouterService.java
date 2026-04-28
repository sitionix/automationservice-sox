package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private final AutomationCapabilitiesProperties capabilitiesProperties;
    private final ObjectMapper objectMapper;

    public List<CapabilityDefinition> discover(final UUID agentId, final UUID conversationId, final String userIntent) {
        final List<CapabilityDefinition> candidates = Arrays.stream(CapabilityName.values())
                .map(CapabilityName::definition)
                .toList();
        log.info(
                "[CAPABILITY] router started userIntentLength={} candidatesCount={} candidateNames={}",
                userIntent == null ? 0 : userIntent.length(),
                candidates.size(),
                candidates.stream().map(CapabilityDefinition::name).toList()
        );
        final List<CapabilityCatalogItem> compactCatalog = candidates.stream()
                .map(definition -> new CapabilityCatalogItem(definition.name(), definition.description(), definition.tags()))
                .toList();
        final String input = this.serializeRouterInput(new CapabilityRouterRequest(userIntent, compactCatalog));
        try {
            final String raw = this.openAiChatClient.execute(new OpenAiChatRequest(ROUTER_INSTRUCTION, input));
            final List<CapabilityDefinition> selectedCapabilities = this.parseSelectedCapabilities(raw, candidates);
            log.info("[CAPABILITY] router selected capabilities={}", selectedCapabilities.stream().map(CapabilityDefinition::name).toList());
            return selectedCapabilities;
        } catch (RuntimeException exception) {
            log.warn(
                    "[CAPABILITY] router failed error={} agentId={} conversationId={}",
                    exception.getMessage(),
                    agentId,
                    conversationId
            );
            return List.of();
        }
    }

    private List<CapabilityDefinition> parseSelectedCapabilities(final String raw, final List<CapabilityDefinition> candidates) {
        final CapabilityRouterResponse routerResponse;
        try {
            routerResponse = this.objectMapper.readValue(raw, CapabilityRouterResponse.class);
        } catch (Exception exception) {
            log.warn("[CAPABILITY] router invalid response");
            return List.of();
        }
        if (routerResponse.capabilities() == null) {
            return List.of();
        }
        final int max = this.capabilitiesProperties.getDiscovery().getMaxSelectedCapabilities();
        final Set<String> selected = new LinkedHashSet<>();
        for (final String name : routerResponse.capabilities()) {
            if (name == null || name.isBlank()) {
                continue;
            }
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

    private String serializeRouterInput(final CapabilityRouterRequest request) {
        try {
            return this.objectMapper.writeValueAsString(request);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize router input", exception);
        }
    }

    private record CapabilityCatalogItem(
            String name,
            String description,
            List<String> tags
    ) {
    }

    private record CapabilityRouterRequest(
            String userIntent,
            List<CapabilityCatalogItem> capabilities
    ) {
    }

    private record CapabilityRouterResponse(
            List<String> capabilities
    ) {
    }
}
