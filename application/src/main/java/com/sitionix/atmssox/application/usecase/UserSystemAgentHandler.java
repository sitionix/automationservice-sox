package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import org.springframework.stereotype.Component;

@Component
public class UserSystemAgentHandler implements SystemAgentHandler {

    private final OpenAiChatClient openAiChatClient;

    public UserSystemAgentHandler(final OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
    public String execute(final Agent agent, final SystemAgentContext context) {
        if (!(context instanceof UserSystemAgentContext userContext)) {
            throw new AgentValidationException("UserSystemAgentContext is required");
        }

        final String instruction = this.normalize(agent.getInstruction());
        if (instruction.isEmpty()) {
            throw new AgentValidationException("User agent instruction is empty");
        }

        final String prompt = this.normalize(userContext.prompt());
        if (prompt.isEmpty()) {
            throw new AgentValidationException("User prompt is empty");
        }

        return this.openAiChatClient.execute(instruction, prompt);
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
