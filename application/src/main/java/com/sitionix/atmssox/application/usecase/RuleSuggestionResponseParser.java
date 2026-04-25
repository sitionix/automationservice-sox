package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RuleSuggestionResponseParser {

    private final ObjectMapper objectMapper;

    public List<RuleSuggestionCandidate> parse(final String rawResponse) {
        if (!StringUtils.hasText(rawResponse)) {
            return List.of();
        }
        try {
            final SuggestionsPayload payload = this.objectMapper.readValue(rawResponse, SuggestionsPayload.class);
            if (payload == null || payload.suggestions() == null) {
                return List.of();
            }
            return payload.suggestions().stream()
                    .map(item -> new RuleSuggestionCandidate(item.title(), item.content(), item.reason()))
                    .toList();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse analyzer JSON response", exception);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SuggestionsPayload(List<SuggestionPayload> suggestions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SuggestionPayload(
            String title,
            String content,
            String reason
    ) {
    }
}
