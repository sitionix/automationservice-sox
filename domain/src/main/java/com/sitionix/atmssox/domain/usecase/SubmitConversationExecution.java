package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatExecution;
import java.util.UUID;

public interface SubmitConversationExecution {

    ChatExecution execute(UUID conversationId, String message);
}
