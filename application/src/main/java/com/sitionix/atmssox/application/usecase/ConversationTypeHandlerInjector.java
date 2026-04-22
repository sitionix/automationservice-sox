package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationTypeHandlerInjector {

    private final ApplicationContext context;

    @PostConstruct
    public void injectHandlers() {
        @SuppressWarnings("unchecked")
        final Map<String, ? extends ConversationChatHandler> handlers =
                (Map<String, ? extends ConversationChatHandler>) (Map<?, ?>) this.context.getBeansOfType(ConversationChatHandler.class);

        Arrays.stream(ConversationType.values())
                .forEach(type -> {
                    final ConversationChatHandler handler = handlers.get(type.getBindingKey());
                    if (handler == null) {
                        throw new IllegalStateException("No ConversationChatHandler bean for type: " + type.getBindingKey());
                    }
                    type.setHandler(handler);
                });
    }
}
