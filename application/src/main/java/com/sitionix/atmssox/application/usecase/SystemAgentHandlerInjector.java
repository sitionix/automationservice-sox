package com.sitionix.atmssox.application.usecase;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemAgentHandlerInjector {

    private final ApplicationContext context;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void injectHandlers() {
        final Map<String, ? extends SystemAgentHandler> handlers =
                (Map<String, ? extends SystemAgentHandler>) (Map<?, ?>) this.context.getBeansOfType(SystemAgentHandler.class);

        Arrays.stream(SystemAgentExecutionType.values())
                .forEach(type -> {
                    final SystemAgentHandler handler = handlers.get(type.getBindingKey());
                    if (handler == null) {
                        throw new IllegalStateException("No SystemAgentHandler bean for type: " + type.getBindingKey());
                    }
                    type.setHandler(handler);
                });
    }
}
