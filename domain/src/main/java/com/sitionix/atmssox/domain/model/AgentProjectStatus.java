package com.sitionix.atmssox.domain.model;

import java.util.Arrays;

public enum AgentProjectStatus {
    ACTIVE(1L),
    ARCHIVED(2L),
    DELETED(3L);

    private final Long id;

    AgentProjectStatus(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public static AgentProjectStatus fromId(final Long id) {
        if (id == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentProjectStatus id: " + id));
    }
}
