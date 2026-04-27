package com.sitionix.atmssox.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextOptimizerPostChatWorkflow implements PostChatWorkflow {

    private final ContextOptimizerTrigger contextOptimizerTrigger;

    @Override
    public void submit(final ChatCompletedContext context) {
        log.debug(
                "Context optimizer workflow invoked for conversationId={}, agentId={}",
                context.conversationId(),
                context.agentId()
        );
        this.contextOptimizerTrigger.submitIfAllowed(context.agentId(), context.conversationId());
    }
}
