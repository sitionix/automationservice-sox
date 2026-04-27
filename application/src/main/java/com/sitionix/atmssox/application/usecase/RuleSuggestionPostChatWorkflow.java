package com.sitionix.atmssox.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleSuggestionPostChatWorkflow implements PostChatWorkflow {

    private final RuleSuggestionAnalysisTrigger ruleSuggestionAnalysisTrigger;

    @Override
    public void submit(final ChatCompletedContext context) {
        log.debug(
                "Rule suggestion workflow invoked for conversationId={}, agentId={}",
                context.conversationId(),
                context.agentId()
        );
        this.ruleSuggestionAnalysisTrigger.submitIfAllowed(
                context.agentId(),
                context.conversationId(),
                context.latestUserMessage()
        );
    }
}
