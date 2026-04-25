package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentType;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentExecutionHandlerRegistry {

    private final List<AgentExecutionHandler<?>> handlers;
    private final Map<AgentType, AgentExecutionHandler<?>> handlersByType = new EnumMap<>(AgentType.class);

    @PostConstruct
    public void injectHandlers() {
        this.handlers.forEach(handler -> {
            final AgentExecutionHandler<?> existing = this.handlersByType.put(handler.supportedAgentType(), handler);
            if (existing != null) {
                throw new IllegalStateException("Duplicate AgentExecutionHandler for type: " + handler.supportedAgentType());
            }
        });
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
