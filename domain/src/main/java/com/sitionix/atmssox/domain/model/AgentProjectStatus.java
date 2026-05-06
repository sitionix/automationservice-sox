package com.sitionix.atmssox.domain.model;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentProjectStatus {
    ACTIVE(1L),
    ARCHIVED(2L),
    DELETED(3L);

    private final Long id;

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
