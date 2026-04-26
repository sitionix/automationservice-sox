package com.sitionix.atmssox.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleSuggestionPostChatWorkflow implements PostChatWorkflow {

    private final RuleSuggestionAnalysisTrigger ruleSuggestionAnalysisTrigger;

    @Override
    public void submit(final ChatCompletedContext context) {
        this.ruleSuggestionAnalysisTrigger.submitIfAllowed(
                context.agentId(),
                context.conversationId(),
                context.latestUserMessage()
        );
    }
}
