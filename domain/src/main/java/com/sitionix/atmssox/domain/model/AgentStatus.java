package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentStatus {
    DRAFT(1L),
    ACTIVE(2L),
    ARCHIVED(3L);

    private final Long id;

    public static AgentStatus fromId(final Long id) {
        if (id == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentStatus id: " + id));
    }

    public AgentStatus activate() {
        if (this == DRAFT) {
            return ACTIVE;
        }
        throw new AgentLifecycleTransitionException("Invalid agent transition: " + this + " -> ACTIVE");
    }

    public AgentStatus archive() {
        if (this == ACTIVE) {
            return ARCHIVED;
        }
        throw new AgentLifecycleTransitionException("Invalid agent transition: " + this + " -> ARCHIVED");
    }
}
