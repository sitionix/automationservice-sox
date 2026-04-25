package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleSuggestionAnalysisTrigger {

    private final RuleSuggestionAnalysisPolicy ruleSuggestionAnalysisPolicy;
    private final RuleSuggestionAnalyzerAsyncService ruleSuggestionAnalyzerAsyncService;

    public void submitIfAllowed(final UUID agentId,
                                final UUID conversationId,
                                final ConversationMessage latestUserMessage) {
        try {
            if (this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage)) {
                this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);
            }
        } catch (TaskRejectedException | IllegalStateException exception) {
            log.warn("Rule suggestion analyzer scheduling failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }
}
