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
            final boolean shouldOptimize = this.contextOptimizerPolicy.shouldOptimize(agentId, conversationId);
            if (shouldOptimize) {
                log.debug("Context optimizer accepted by policy for agentId={}, conversationId={}", agentId, conversationId);
                this.contextOptimizerAsyncService.optimizeAsync(agentId, conversationId);
                log.debug("Context optimizer async submitted for agentId={}, conversationId={}", agentId, conversationId);
            } else {
                log.debug("Context optimizer skipped by policy for agentId={}, conversationId={}", agentId, conversationId);
            }
        } catch (TaskRejectedException | IllegalStateException exception) {
            log.warn("Context optimizer scheduling failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }
}
