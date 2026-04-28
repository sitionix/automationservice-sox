package com.sitionix.atmssox.domain.model.capability;

import java.util.UUID;

public record CapabilityExecutionCommand<H>(
        Long userId,
        Long agentId,
        UUID conversationId,
        H argNode
) {
}
