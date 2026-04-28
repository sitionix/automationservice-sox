package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapabilityHandlerInjector {

    private final ApplicationContext context;

    @PostConstruct
    public void injectHandlers() {
        @SuppressWarnings("unchecked")
        final Map<String, ? extends CapabilityHandler> handlers =
                (Map<String, ? extends CapabilityHandler>) (Map<?, ?>) this.context.getBeansOfType(CapabilityHandler.class);

        Arrays.stream(CapabilityName.values())
                .forEach(type -> {
                    final CapabilityHandler handler = handlers.get(type.getBindingKey());
                    if (handler == null) {
                        throw new IllegalStateException("No CapabilityHandler bean for type: " + type.getBindingKey());
                    }
                    type.setHandler(handler);
                });
    }
}
