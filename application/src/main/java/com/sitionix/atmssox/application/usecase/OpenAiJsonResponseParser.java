package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiJsonResponseParser {

    private final ObjectMapper objectMapper;

    public Optional<JsonNode> parseObject(final String rawResponse) {
        try {
            return Optional.of(this.objectMapper.readTree(rawResponse));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public <T> List<T> parseArrayField(final JsonNode root,
                                       final String fieldName,
                                       final TypeReference<List<T>> typeReference) {
        if (root == null) {
            return List.of();
        }
        final JsonNode node = root.path(fieldName);
        if (!node.isArray()) {
            return List.of();
        }
        try {
            return this.objectMapper.convertValue(node, typeReference);
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
    }
}
