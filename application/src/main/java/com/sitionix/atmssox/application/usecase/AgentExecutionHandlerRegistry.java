package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentType;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentExecutionHandlerRegistry {

    private final ApplicationContext context;
    private final Map<AgentType, AgentExecutionHandler> handlersByType = new EnumMap<>(AgentType.class);

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void injectHandlers() {
        final Map<String, ? extends AgentExecutionHandler> handlers =
                (Map<String, ? extends AgentExecutionHandler>) (Map<?, ?>) this.context.getBeansOfType(AgentExecutionHandler.class);

        Arrays.stream(AgentType.values())
                .forEach(type -> {
                    final AgentExecutionHandler handler = handlers.get(type.getBindingKey());
                    if (handler == null) {
                        throw new IllegalStateException("No AgentExecutionHandler bean for type: " + type.getBindingKey());
                    }
                    this.handlersByType.put(type, handler);
                });
    }

    public AgentExecutionHandler getHandler(final AgentType type) {
        final AgentExecutionHandler handler = this.handlersByType.get(type);
        if (handler == null) {
            throw new AgentValidationException("Agent execution handler is not configured for agent type: " + type);
        }
        return handler;
    }
}
