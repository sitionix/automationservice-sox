package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentType;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentExecutionHandlerRegistry {

    private final UserAgentExecutionHandler userAgentExecutionHandler;
    private final RuleSuggestionAnalyzerAgentExecutionHandler ruleSuggestionAnalyzerAgentExecutionHandler;
    private final Map<AgentType, AgentExecutionHandler<?>> handlersByType = new EnumMap<>(AgentType.class);

    @PostConstruct
    public void injectHandlers() {
        this.handlersByType.put(AgentType.USER, this.userAgentExecutionHandler);
        this.handlersByType.put(AgentType.SYSTEM_RULE_ANALYZER, this.ruleSuggestionAnalyzerAgentExecutionHandler);
        for (final AgentType type : AgentType.values()) {
            if (!this.handlersByType.containsKey(type)) {
                throw new IllegalStateException("No AgentExecutionHandler bean for type: " + type.name());
            }
        }
    }

    public AgentExecutionHandler<?> getHandler(final AgentType type) {
        final AgentExecutionHandler<?> handler = this.handlersByType.get(type);
        if (handler == null) {
            throw new AgentValidationException("Agent execution handler is not configured for agent type: " + type);
        }
        return handler;
    }
}
