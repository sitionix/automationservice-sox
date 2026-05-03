package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ChatExecution {

    UUID executionId;

    UUID agentId;

    UUID conversationId;

    Long userId;

    ChatExecutionStatus status;

    String requestMessage;

    String idempotencyKey;

    boolean idempotencyReplayed;

    UUID inputMessageId;

    UUID assistantMessageId;

    ConversationMessage assistantMessage;

    ChatExecutionFailure failure;

    Instant createdAt;

    Instant startedAt;

    Instant completedAt;
}
