package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.TextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAgentExecutionHandler implements AgentExecutionHandler<UserAgentExecutionContext> {

    private final OpenAiChatClient openAiChatClient;

    @Override
    public Class<UserAgentExecutionContext> supportedContextType() {
        return UserAgentExecutionContext.class;
    }

    @Override
    public String execute(final Agent agent, final UserAgentExecutionContext context) {
        final String instruction = TextNormalizer.normalizeToEmpty(agent.getInstruction());
        final String prompt = TextNormalizer.normalizeToEmpty(context.prompt());
        if (prompt.isEmpty()) {
            throw new AgentValidationException("User prompt is empty");
        }

        return this.openAiChatClient.execute(new OpenAiChatRequest(instruction, prompt));
    }
}
