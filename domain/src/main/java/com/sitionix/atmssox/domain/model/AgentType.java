package com.sitionix.atmssox.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgentType {
    USER(1L, "userAgentExecutionHandler"),
    SYSTEM_RULE_ANALYZER(2L, "ruleSuggestionAnalyzerAgentExecutionHandler");

    private final Long id;
    private final String bindingKey;

    public static AgentType fromId(final Long id) {
        return java.util.Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentType id: " + id));
    }
}
