package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContextOptimizerAsyncService {

    private final AgentRepository agentRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationContextSnapshotRepository conversationContextSnapshotRepository;
    private final AgentExecutionService agentExecutionService;
    private final ContextOptimizerProperties properties;
    private final OpenAiJsonResponseParser openAiJsonResponseParser;
    private final ActiveUserAgentResolver activeUserAgentResolver;
    private final ContextOptimizationCoverageCalculator contextOptimizationCoverageCalculator;

    @Async("contextOptimizerTaskExecutor")
    public void optimizeAsync(final UUID agentId, final UUID conversationId) {
        final Optional<Agent> optimizerAgent = this.findActiveOptimizer();
        if (optimizerAgent.isEmpty()) {
            log.warn("Context optimizer agent missing or inactive for conversationId={}", conversationId);
            return;
        }
        final Optional<Agent> targetAgent = this.findActiveUserAgent(agentId);
        if (targetAgent.isEmpty()) {
            log.warn("Target USER agent missing or inactive for agentId={}, conversationId={}", agentId, conversationId);
            return;
        }

        final List<ConversationMessage> fullHistory = this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        if (fullHistory.isEmpty()) {
            return;
        }

        final Optional<ConversationContextSnapshot> existingSnapshot = this.conversationContextSnapshotRepository.findByConversationId(conversationId);
        final int alreadyCoveredCount = existingSnapshot.map(ConversationContextSnapshot::getMessageCountUntil).orElse(0);
        final int compressUntilCount = this.contextOptimizationCoverageCalculator.resolveCompressUntilCount(
                fullHistory.size(),
                this.properties.getLastMessagesLimit()
        );
        if (!this.contextOptimizationCoverageCalculator.hasNewCoverage(alreadyCoveredCount, compressUntilCount)) {
            return;
        }

        final List<ConversationMessage> messagesToSummarize = fullHistory.subList(alreadyCoveredCount, compressUntilCount);
        final ContextOptimizationContext context = new ContextOptimizationContext(
                targetAgent.get().getInstruction(),
                existingSnapshot.map(ConversationContextSnapshot::getSummary).orElse(""),
                messagesToSummarize
        );

        final Optional<String> summary = this.executeAndParseSummary(
                optimizerAgent.get(),
                context,
                agentId,
                conversationId
        );
        if (summary.isEmpty()) {
            return;
        }
        this.saveSnapshot(existingSnapshot, conversationId, summary.get(), fullHistory, compressUntilCount);
    }

    private Optional<Agent> findActiveOptimizer() {
        return this.agentRepository.findSystemContextOptimizer()
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE);
    }

    private Optional<Agent> findActiveUserAgent(final UUID agentId) {
        return this.activeUserAgentResolver.findById(agentId);
    }

    private Optional<String> executeAndParseSummary(final Agent optimizerAgent,
                                                    final ContextOptimizationContext context,
                                                    final UUID agentId,
                                                    final UUID conversationId) {
        try {
            final String rawResponse = this.agentExecutionService.execute(optimizerAgent, context);
            return this.parseSummary(rawResponse, agentId, conversationId);
        } catch (OpenAiExecutionException exception) {
            log.warn("Context optimizer OpenAI execution failed for agentId={}, conversationId={}", agentId, conversationId, exception);
            return Optional.empty();
        }
    }

    private Optional<String> parseSummary(final String rawResponse,
                                          final UUID agentId,
                                          final UUID conversationId) {
        final Optional<JsonNode> root = this.openAiJsonResponseParser.parseObject(rawResponse);
        if (root.isEmpty()) {
            log.warn("Context optimizer returned invalid JSON for agentId={}, conversationId={}", agentId, conversationId);
            return Optional.empty();
        }
        final JsonNode summaryNode = root.get().get("summary");
        if (summaryNode == null || !summaryNode.isTextual()) {
            log.warn("Context optimizer response has no textual summary for agentId={}, conversationId={}", agentId, conversationId);
            return Optional.empty();
        }
        final String summary = AgentRuleTextNormalizer.normalizeToEmpty(summaryNode.asText());
        if (summary.isEmpty()) {
            return Optional.empty();
        }
        if (summary.length() > this.properties.getMaxSummaryLength()) {
            log.warn(
                    "Context optimizer summary exceeds max length for agentId={}, conversationId={}, actualLength={}, maxLength={}",
                    agentId,
                    conversationId,
                    summary.length(),
                    this.properties.getMaxSummaryLength()
            );
            return Optional.empty();
        }
        return Optional.of(summary);
    }

    private void saveSnapshot(final Optional<ConversationContextSnapshot> existingSnapshot,
                              final UUID conversationId,
                              final String summary,
                              final List<ConversationMessage> fullHistory,
                              final int compressUntilCount) {
        final Instant now = Instant.now();
        final UUID lastMessageIdUntil = compressUntilCount > 0
                ? fullHistory.get(compressUntilCount - 1).getId()
                : null;
        final ConversationContextSnapshot snapshot = existingSnapshot
                .map(value -> value.toBuilder()
                        .summary(summary)
                        .messageCountUntil(compressUntilCount)
                        .lastMessageIdUntil(lastMessageIdUntil)
                        .updatedAt(now)
                        .build())
                .orElseGet(() -> ConversationContextSnapshot.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .summary(summary)
                        .messageCountUntil(compressUntilCount)
                        .lastMessageIdUntil(lastMessageIdUntil)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
        try {
            this.conversationContextSnapshotRepository.save(snapshot);
        } catch (RuntimeException exception) {
            log.warn("Context snapshot save failed for conversationId={}", conversationId, exception);
        }
    }
}
