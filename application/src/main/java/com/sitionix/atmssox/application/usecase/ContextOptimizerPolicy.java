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
import org.springframework.stereotype.Component;

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
            return false;
        }
        final Optional<Agent> optimizerAgent = this.agentRepository.findSystemContextOptimizer();
        if (optimizerAgent.isEmpty() || optimizerAgent.get().getStatus() != AgentStatus.ACTIVE) {
            return false;
        }

        if (this.activeUserAgentResolver.findById(agentId).isEmpty()) {
            return false;
        }

        final long totalMessageCount = this.conversationMessageRepository.countByConversationId(conversationId);
        if (totalMessageCount <= (long) this.properties.getLastMessagesLimit() + this.properties.getOptimizeThresholdMessages()) {
            return false;
        }
        final int compressUntilCount = this.contextOptimizationCoverageCalculator.resolveCompressUntilCount(
                totalMessageCount,
                this.properties.getLastMessagesLimit()
        );

        final Optional<ConversationContextSnapshot> snapshot = this.conversationContextSnapshotRepository.findByConversationId(conversationId);
        if (snapshot.isEmpty()) {
            return true;
        }
        if (!this.contextOptimizationCoverageCalculator.hasNewCoverage(
                snapshot.get().getMessageCountUntil(),
                compressUntilCount
        )) {
            return false;
        }
        final Instant nextAllowedAt = snapshot.get()
                .getUpdatedAt()
                .plusSeconds((long) this.properties.getConversationCooldownMinutes() * 60L);
        return !nextAllowedAt.isAfter(Instant.now());
    }
}
