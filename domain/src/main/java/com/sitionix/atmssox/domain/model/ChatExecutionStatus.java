package com.sitionix.atmssox.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatExecutionStatus {
    QUEUED(1L),
    IN_PROGRESS(2L),
    COMPLETED(3L),
    FAILED(4L),
    DISPATCH_SKIPPED(5L);

    private final Long id;

    public static ChatExecutionStatus fromId(final Long id) {
        return Arrays.stream(values())
                .filter(status -> status.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ChatExecutionStatus id: " + id));
    }
}
