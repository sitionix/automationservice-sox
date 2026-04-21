package com.sitionix.atmssox.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatAgentCommand {

    UUID conversationId;

    String message;
}
