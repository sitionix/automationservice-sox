package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.UUID;

public record ChatCompletedContext(
        UUID agentId,
        UUID conversationId,
        ConversationMessage latestUserMessage
) {
}
