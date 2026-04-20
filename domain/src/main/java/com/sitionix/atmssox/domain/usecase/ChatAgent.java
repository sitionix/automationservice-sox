package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import java.util.UUID;

/**
 * Executes one chat request for one automation agent.
 */
public interface ChatAgent {

    /**
     * Executes one request-response chat interaction.
     *
     * @param agentId unique agent identifier.
     * @param command chat request payload.
     * @return normalized assistant reply payload.
     */
    ChatAgentResponse execute(UUID agentId, ChatAgentCommand command);
}
