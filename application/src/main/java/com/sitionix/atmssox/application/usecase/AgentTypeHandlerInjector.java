package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentTypeHandlerInjector {

    private final ApplicationContext context;

    @PostConstruct
    public void injectHandlers() {
        @SuppressWarnings("unchecked")
        final Map<String, ? extends AgentExecutionHandler<?>> handlers =
                (Map<String, ? extends AgentExecutionHandler<?>>) (Map<?, ?>) this.context.getBeansOfType(AgentExecutionHandler.class);

        Arrays.stream(AgentType.values())
                .forEach(type -> {
                    final AgentExecutionHandler<?> handler = handlers.get(type.getBindingKey());
                    if (handler == null) {
                        throw new IllegalStateException("No AgentExecutionHandler bean for type: " + type.getBindingKey());
                    }
                    type.setHandler(handler);
                });
    }
}
