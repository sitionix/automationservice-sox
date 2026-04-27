package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleSuggestionAnalysisPolicy {

    private final RuleSuggestionAnalyzerProperties properties;
    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    public boolean shouldAnalyze(final UUID agentId,
                                 final UUID conversationId,
                                 final ConversationMessage latestUserMessage) {
        if (!this.properties.isEnabled()) {
            log.debug("Rule suggestion policy denied: feature disabled for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }
        if (latestUserMessage == null || latestUserMessage.getAuthorType() != ConversationParticipantType.USER) {
            log.debug("Rule suggestion policy denied: latest user message missing/invalid for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }

        final Optional<Agent> targetAgent = this.agentRepository.findById(agentId);
        if (targetAgent.isEmpty()
                || targetAgent.get().getType() != AgentType.USER
                || targetAgent.get().getStatus() != AgentStatus.ACTIVE) {
            log.debug("Rule suggestion policy denied: target agent is not active USER for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }

        final long pendingSuggestions = this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(
                agentId,
                AgentRuleStatus.PENDING,
                AgentRuleAuthorType.AI
        );
        final int maxPendingSuggestionsPerAgent = this.properties.getMaxPendingSuggestionsPerAgent();
        if (pendingSuggestions >= maxPendingSuggestionsPerAgent) {
            log.debug(
                    "Rule suggestion policy denied: pending suggestions limit reached for agentId={}, conversationId={}, pendingSuggestions={}, maxPendingSuggestions={}",
                    agentId,
                    conversationId,
                    pendingSuggestions,
                    maxPendingSuggestionsPerAgent
            );
            return false;
        }

        final Instant now = Instant.now();
        if (!this.isCooldownPassed(agentId, now)) {
            log.debug("Rule suggestion policy denied: cooldown not passed for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }
        if (!this.isDailyQuotaAvailable(agentId, now)) {
            log.debug("Rule suggestion policy denied: daily quota exhausted for agentId={}, conversationId={}", agentId, conversationId);
            return false;
        }

        final long userMessageCount = this.conversationMessageRepository.countByConversationIdAndAuthorType(
                conversationId,
                ConversationParticipantType.USER
        );
        final int messageCountThreshold = this.properties.getMessageCountThreshold();
        final boolean allowed = userMessageCount >= messageCountThreshold;
        if (!allowed) {
            log.debug(
                    "Rule suggestion policy denied: user message threshold not reached for agentId={}, conversationId={}, userMessageCount={}, threshold={}",
                    agentId,
                    conversationId,
                    userMessageCount,
                    messageCountThreshold
            );
            return false;
        }
        log.debug(
                "Rule suggestion policy allowed for agentId={}, conversationId={}, userMessageCount={}",
                agentId,
                conversationId,
                userMessageCount
        );
        return true;
    }

    private boolean isCooldownPassed(final UUID agentId, final Instant now) {
        final Optional<Instant> lastAiSuggestionCreatedAt =
                this.agentRuleRepository.findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI);
        if (lastAiSuggestionCreatedAt.isEmpty()) {
            return true;
        }
        return lastAiSuggestionCreatedAt.get()
                .plusSeconds(this.properties.getConversationCooldownMinutes() * 60L)
                .isBefore(now);
    }

    private boolean isDailyQuotaAvailable(final UUID agentId, final Instant now) {
        final LocalDate utcDate = LocalDate.ofInstant(now, ZoneOffset.UTC);
        final Instant dayStartUtc = utcDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        final Instant nextDayStartUtc = utcDate.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        final long analysesToday = this.agentRuleRepository.countByAgentIdAndAuthorTypeAndCreatedAtBetween(
                agentId,
                AgentRuleAuthorType.AI,
                dayStartUtc,
                nextDayStartUtc
        );
        return analysesToday < this.properties.getMaxAgentAnalysesPerDay();
    }
}
