package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ConversationContextSnapshot {

    UUID id;

    UUID conversationId;

    String summary;

    int messageCountUntil;

    UUID lastMessageIdUntil;

    Instant createdAt;

    Instant updatedAt;
}
