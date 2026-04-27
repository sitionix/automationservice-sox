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
            final boolean shouldAnalyze = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage);
            if (shouldAnalyze) {
                log.debug("Rule suggestion analyzer accepted by policy for agentId={}, conversationId={}", agentId, conversationId);
                this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);
                log.debug("Rule suggestion analyzer async submitted for agentId={}, conversationId={}", agentId, conversationId);
            } else {
                log.debug("Rule suggestion analyzer skipped by policy for agentId={}, conversationId={}", agentId, conversationId);
            }
        } catch (TaskRejectedException | IllegalStateException exception) {
            log.warn("Rule suggestion analyzer scheduling failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }
}
