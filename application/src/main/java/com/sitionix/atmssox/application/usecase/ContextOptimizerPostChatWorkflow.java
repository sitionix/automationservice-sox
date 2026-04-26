package com.sitionix.atmssox.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContextOptimizerPostChatWorkflow implements PostChatWorkflow {

    private final ContextOptimizerTrigger contextOptimizerTrigger;

    @Override
    public void submit(final ChatCompletedContext context) {
        this.contextOptimizerTrigger.submitIfAllowed(context.agentId(), context.conversationId());
    }
}
