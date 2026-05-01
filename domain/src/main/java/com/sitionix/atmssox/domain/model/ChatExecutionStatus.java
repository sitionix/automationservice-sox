package com.sitionix.atmssox.domain.model;

import java.util.Arrays;

public enum ChatExecutionStatus {
    QUEUED(1L),
    IN_PROGRESS(2L),
    COMPLETED(3L),
    FAILED(4L);

    private final Long id;

    ChatExecutionStatus(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public static ChatExecutionStatus fromId(final Long id) {
        return Arrays.stream(values())
                .filter(status -> status.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ChatExecutionStatus id: " + id));
    }
}
