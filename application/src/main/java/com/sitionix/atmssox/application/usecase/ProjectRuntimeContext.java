package com.sitionix.atmssox.application.usecase;

import java.util.UUID;

public record ProjectRuntimeContext(
        UUID projectId,
        String projectName,
        String projectContextText
) {
}
