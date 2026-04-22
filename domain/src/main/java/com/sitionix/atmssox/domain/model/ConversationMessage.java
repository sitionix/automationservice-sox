package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ConversationMessage {

    UUID id;

    UUID conversationId;

    ConversationParticipantType authorType;

    String authorId;

    String content;

    Instant createdAt;
}
