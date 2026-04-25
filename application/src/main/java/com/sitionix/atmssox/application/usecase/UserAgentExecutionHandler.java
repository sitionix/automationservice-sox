package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import org.springframework.stereotype.Component;

@Component
public class UserAgentExecutionHandler implements AgentExecutionHandler<UserAgentExecutionContext> {

    private final OpenAiChatClient openAiChatClient;

    public UserAgentExecutionHandler(final OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
    public Class<UserAgentExecutionContext> supportedContextType() {
        return UserAgentExecutionContext.class;
    }

    @Override
    public String execute(final Agent agent, final UserAgentExecutionContext context) {
        final String instruction = this.normalize(agent.getInstruction());
        final String prompt = this.normalize(context.prompt());
        if (prompt.isEmpty()) {
            throw new AgentValidationException("User prompt is empty");
        }

        return this.openAiChatClient.execute(new OpenAiChatRequest(instruction, prompt));
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
