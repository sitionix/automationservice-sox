package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum AgentType {
    USER(1L, "userAgentExecutionHandler"),
    SYSTEM_RULE_ANALYZER(2L, "ruleSuggestionAnalyzerAgentExecutionHandler");

    private final Long id;
    private final String bindingKey;

    @Setter
    private AgentExecutionHandler<?> handler;

    public String execute(final Agent agent, final AgentExecutionContext context) {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for agent type: " + this.name());
        }
        return this.handler.executeWithContext(agent, context);
    }

    public Class<? extends AgentExecutionContext> supportedContextType() {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for agent type: " + this.name());
        }
        return this.handler.supportedContextType();
    }

    public static AgentType fromId(final Long id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown AgentType id: " + id));
    }
}
