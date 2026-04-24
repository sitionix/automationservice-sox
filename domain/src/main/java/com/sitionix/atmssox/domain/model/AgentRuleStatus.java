package com.sitionix.atmssox.domain.model;

import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentRuleStatus {

    ACTIVE(1L),
    DELETED(2L);

    private final Long id;

    public static AgentRuleStatus fromId(final Long id) {
        if (id == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentRuleStatus id: " + id));
    }

    public AgentRuleStatus delete() {
        return DELETED;
    }
}
