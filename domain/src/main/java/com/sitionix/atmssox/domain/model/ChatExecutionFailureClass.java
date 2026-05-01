package com.sitionix.atmssox.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatExecutionFailureClass {
    OWNERSHIP_VIOLATION(1L),
    CONVERSATION_NOT_FOUND(2L),
    INVALID_LIFECYCLE_STATE(3L),
    IDEMPOTENCY_CONFLICT(4L),
    EXECUTION_ERROR(5L);

    private final Long id;

    public static ChatExecutionFailureClass fromId(final Long id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ChatExecutionFailureClass id: " + id));
    }
}
