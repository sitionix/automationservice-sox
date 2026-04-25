package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentType;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum SystemAgentExecutionType {
    USER(AgentType.USER, "userSystemAgentHandler"),
    RULE_SUGGESTION_ANALYZER(AgentType.SYSTEM_RULE_ANALYZER, "ruleSuggestionAnalyzerSystemAgentHandler");

    private final AgentType agentType;
    private final String bindingKey;

    @Setter
    private SystemAgentHandler handler;

    public String execute(final Agent agent, final SystemAgentContext context) {
        if (this.handler == null) {
            throw new IllegalStateException("No SystemAgentHandler configured for type: " + this.name());
        }
        return this.handler.execute(agent, context);
    }

    public static SystemAgentExecutionType fromAgentType(final AgentType agentType) {
        return Arrays.stream(values())
                .filter(type -> type.agentType == agentType)
                .findFirst()
                .orElseThrow(() -> new AgentValidationException("System agent execution type is not configured for agent type: " + agentType));
    }
}
