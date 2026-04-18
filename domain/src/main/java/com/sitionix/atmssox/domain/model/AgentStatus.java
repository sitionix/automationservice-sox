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
    ARCHIVED(3L),
    DELETED(4L);

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
        if (this == DRAFT || this == ACTIVE) {
            return ARCHIVED;
        }
        throw new AgentLifecycleTransitionException("Invalid agent transition: " + this + " -> ARCHIVED");
    }

    public AgentStatus restore() {
        if (this == ARCHIVED) {
            return DRAFT;
        }
        throw new AgentLifecycleTransitionException("Invalid agent transition: " + this + " -> DRAFT");
    }

    public AgentStatus delete() {
        if (this == DRAFT || this == ACTIVE || this == ARCHIVED) {
            return DELETED;
        }
        throw new AgentLifecycleTransitionException("Invalid agent transition: " + this + " -> DELETED");
    }
}
