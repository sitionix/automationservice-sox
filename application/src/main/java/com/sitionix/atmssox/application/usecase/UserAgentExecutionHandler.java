package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import org.springframework.stereotype.Component;

@Component
public class UserAgentExecutionHandler implements AgentExecutionHandler {

    private final OpenAiChatClient openAiChatClient;

    public UserAgentExecutionHandler(final OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
    public String execute(final Agent agent, final AgentExecutionContext context) {
        if (!(context instanceof UserAgentExecutionContext userContext)) {
            throw new AgentValidationException("UserAgentExecutionContext is required");
        }

        final String instruction = this.normalize(agent.getInstruction());
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
