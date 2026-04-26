package com.sitionix.atmssox.application.usecase;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextOptimizerTrigger {

    private final ContextOptimizerPolicy contextOptimizerPolicy;
    private final ContextOptimizerAsyncService contextOptimizerAsyncService;

    public void submitIfAllowed(final UUID agentId, final UUID conversationId) {
        try {
            if (this.contextOptimizerPolicy.shouldOptimize(agentId, conversationId)) {
                this.contextOptimizerAsyncService.optimizeAsync(agentId, conversationId);
            }
        } catch (TaskRejectedException | IllegalStateException exception) {
            log.warn("Context optimizer scheduling failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }
}
