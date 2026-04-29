package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatExecution;
import java.util.UUID;

public interface GetAgentChatExecution {

    ChatExecution execute(UUID agentId, UUID executionId, UUID conversationId);
}
