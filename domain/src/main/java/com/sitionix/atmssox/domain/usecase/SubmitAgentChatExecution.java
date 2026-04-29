package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import java.util.UUID;

public interface SubmitAgentChatExecution {

    ChatExecution execute(UUID agentId, ChatAgentCommand command, String idempotencyKey);
}
