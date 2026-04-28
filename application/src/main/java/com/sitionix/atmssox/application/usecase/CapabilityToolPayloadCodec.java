package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapabilityToolPayloadCodec {

    private final ObjectMapper objectMapper;

    public JsonNode parseArgs(final String argumentsJson) {
        try {
            return this.objectMapper.readTree(argumentsJson == null ? "{}" : argumentsJson);
        } catch (JsonProcessingException exception) {
            return this.objectMapper.createObjectNode();
        }
    }

    public String serializeDiscoveredCapabilities(final List<CapabilityDefinition> capabilities) {
        try {
            return this.objectMapper.writeValueAsString(new DiscoveryOutput(capabilities));
        } catch (JsonProcessingException exception) {
            return "{\"capabilities\":[]}";
        }
    }

    public String serializeJsonNode(final JsonNode payload) {
        try {
            return this.objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    public String serializeError(final String message) {
        try {
            return this.objectMapper.writeValueAsString(new ErrorOutput(message));
        } catch (JsonProcessingException exception) {
            return "{\"error\":\"internal_error\"}";
        }
    }

    private record DiscoveryOutput(List<CapabilityDefinition> capabilities) {
    }

    private record ErrorOutput(String error) {
    }
}
