package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Conversation {

    UUID id;

    Long userId;

    UUID projectId;

    String title;

    ConversationType type;

    ConversationStatus status;

    Instant createdAt;

    Instant updatedAt;

    Instant lastMessageAt;
}
