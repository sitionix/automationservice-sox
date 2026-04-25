package com.sitionix.atmssox.domain.model;

public enum AgentRuleAuthorType {
    USER(1L),
    AI(2L);

    private final Long id;

    AgentRuleAuthorType(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public static AgentRuleAuthorType fromId(final Long id) {
        for (final AgentRuleAuthorType value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown AgentRuleAuthorType id: " + id);
    }
}
