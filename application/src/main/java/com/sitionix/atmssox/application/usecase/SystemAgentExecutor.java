package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SystemAgentExecutor {

    private final OpenAiChatClient openAiChatClient;
    private final Map<AgentType, SystemAgentPromptBuilder> buildersByType;

    public SystemAgentExecutor(final OpenAiChatClient openAiChatClient,
                               final List<SystemAgentPromptBuilder> builders) {
        this.openAiChatClient = openAiChatClient;
        this.buildersByType = new EnumMap<>(AgentType.class);
        builders.forEach(builder -> this.buildersByType.put(builder.getAgentType(), builder));
    }

    public String execute(final Agent agent, final SystemAgentContext context) {
        final SystemAgentPromptBuilder builder = this.buildersByType.get(agent.getType());
        if (builder == null) {
            throw new AgentValidationException("System prompt builder is not configured for agent type: " + agent.getType());
        }
        final String instruction = this.normalize(agent.getInstruction());
        if (instruction.isEmpty()) {
            throw new AgentValidationException("System agent instruction is empty");
        }
        return this.openAiChatClient.execute(instruction, builder.build(context));
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
