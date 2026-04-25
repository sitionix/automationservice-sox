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
import org.springframework.stereotype.Component;

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
            return false;
        }
        if (latestUserMessage == null || latestUserMessage.getAuthorType() != ConversationParticipantType.USER) {
            return false;
        }

        final Optional<Agent> targetAgent = this.agentRepository.findById(agentId);
        if (targetAgent.isEmpty()
                || targetAgent.get().getType() != AgentType.USER
                || targetAgent.get().getStatus() != AgentStatus.ACTIVE) {
            return false;
        }

        final long pendingSuggestions = this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(
                agentId,
                AgentRuleStatus.PENDING,
                AgentRuleAuthorType.AI
        );
        if (pendingSuggestions >= this.properties.getMaxPendingSuggestionsPerAgent()) {
            return false;
        }

        final Instant now = Instant.now();
        if (!this.isCooldownPassed(agentId, now)) {
            return false;
        }
        if (!this.isDailyQuotaAvailable(agentId, now)) {
            return false;
        }

        final long userMessageCount = this.conversationMessageRepository.countByConversationIdAndAuthorType(
                conversationId,
                ConversationParticipantType.USER
        );
        if (userMessageCount < this.properties.getMessageCountThreshold()) {
            return false;
        }
        return true;
    }

    private boolean isCooldownPassed(final UUID agentId, final Instant now) {
        final Optional<Instant> lastAiSuggestionCreatedAt =
                this.agentRuleRepository.findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI);
        if (lastAiSuggestionCreatedAt.isEmpty()) {
            return true;
        }
        return lastAiSuggestionCreatedAt.get()
                .plusSeconds((long) this.properties.getConversationCooldownMinutes() * 60L)
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
