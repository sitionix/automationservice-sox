package com.sitionix.atmssox.domain.model;

public enum AgentType {
    USER(1L),
    SYSTEM_RULE_ANALYZER(2L);

    private final Long id;

    AgentType(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public static AgentType fromId(final Long id) {
        return java.util.Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentType id: " + id));
    }
}
