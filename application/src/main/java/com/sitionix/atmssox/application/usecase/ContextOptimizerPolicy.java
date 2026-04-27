package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextOptimizerPolicy {

    private final ContextOptimizerProperties properties;
    private final AgentRepository agentRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationContextSnapshotRepository conversationContextSnapshotRepository;
    private final ActiveUserAgentResolver activeUserAgentResolver;
    private final ContextOptimizationCoverageCalculator contextOptimizationCoverageCalculator;

    public boolean shouldOptimize(final UUID agentId, final UUID conversationId) {
        if (!this.properties.isEnabled()) {
            log.debug("Context optimizer policy denied: feature disabled for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }
        final Optional<Agent> optimizerAgent = this.agentRepository.findSystemContextOptimizer();
        if (optimizerAgent.isEmpty() || optimizerAgent.get().getStatus() != AgentStatus.ACTIVE) {
            log.debug("Context optimizer policy denied: optimizer agent missing/inactive for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }

        if (this.activeUserAgentResolver.findById(agentId).isEmpty()) {
            log.debug("Context optimizer policy denied: target agent is not active USER for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }

        final long totalMessageCount = this.conversationMessageRepository.countByConversationId(conversationId);
        if (totalMessageCount <= (long) this.properties.getLastMessagesLimit() + this.properties.getOptimizeThresholdMessages()) {
            log.debug(
                    "Context optimizer policy denied: total messages below threshold for agentId={}, conversationId={}, totalMessageCount={}, requiredMoreThan={}",
                    agentId,
                    conversationId,
                    totalMessageCount,
                    (long) this.properties.getLastMessagesLimit() + this.properties.getOptimizeThresholdMessages()
            );
            return false;
        }
        final int compressUntilCount = this.contextOptimizationCoverageCalculator.resolveCompressUntilCount(
                totalMessageCount,
                this.properties.getLastMessagesLimit()
        );

        final Optional<ConversationContextSnapshot> snapshot = this.conversationContextSnapshotRepository.findByConversationId(conversationId);
        if (snapshot.isEmpty()) {
            log.debug(
                    "Context optimizer policy allowed for agentId={}, conversationId={} (no snapshot yet, compressUntilCount={})",
                    agentId,
                    conversationId,
                    compressUntilCount
            );
            return true;
        }
        if (!this.contextOptimizationCoverageCalculator.hasNewCoverage(
                snapshot.get().getMessageCountUntil(),
                compressUntilCount
        )) {
            log.debug(
                    "Context optimizer policy denied: no new coverage for agentId={}, conversationId={}, coveredUntil={}, compressUntilCount={}",
                    agentId,
                    conversationId,
                    snapshot.get().getMessageCountUntil(),
                    compressUntilCount
            );
            return false;
        }
        final Instant nextAllowedAt = snapshot.get()
                .getUpdatedAt()
                .plusSeconds((long) this.properties.getConversationCooldownMinutes() * 60L);
        final boolean allowed = !nextAllowedAt.isAfter(Instant.now());
        if (!allowed) {
            log.debug(
                    "Context optimizer policy denied: cooldown not passed for agentId={}, conversationId={}, nextAllowedAt={}",
                    agentId,
                    conversationId,
                    nextAllowedAt
            );
            return false;
        }
        log.debug(
                "Context optimizer policy allowed for agentId={}, conversationId={}, coveredUntil={}, compressUntilCount={}",
                agentId,
                conversationId,
                snapshot.get().getMessageCountUntil(),
                compressUntilCount
        );
        return true;
    }
}
