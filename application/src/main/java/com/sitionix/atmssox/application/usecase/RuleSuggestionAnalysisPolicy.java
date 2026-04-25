package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.RuleSuggestionAnalysisRun;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.RuleSuggestionAnalysisRunRepository;
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
    private final RuleSuggestionAnalysisRunRepository ruleSuggestionAnalysisRunRepository;

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

        final long userMessageCount = this.conversationMessageRepository.countByConversationIdAndAuthorType(
                conversationId,
                ConversationParticipantType.USER
        );
        final Optional<RuleSuggestionAnalysisRun> latestRun = this.ruleSuggestionAnalysisRunRepository
                .findLatestByAgentIdAndConversationId(agentId, conversationId);
        if (latestRun.isPresent()) {
            final Instant cooldownDeadline = latestRun.get().getCreatedAt()
                    .plusSeconds((long) this.properties.getConversationCooldownMinutes() * 60L);
            if (cooldownDeadline.isAfter(Instant.now())) {
                return false;
            }

            final long newUserMessages = userMessageCount - latestRun.get().getUserMessageCount();
            if (newUserMessages < this.properties.getMessageCountThreshold()) {
                return false;
            }
        } else if (userMessageCount < this.properties.getMessageCountThreshold()) {
            return false;
        }

        final Instant dayStartUtc = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        final Instant nextDayStartUtc = dayStartUtc.plusSeconds(24 * 60 * 60L);
        final long analysesToday = this.ruleSuggestionAnalysisRunRepository.countByAgentIdAndCreatedAtBetween(
                agentId,
                dayStartUtc,
                nextDayStartUtc
        );
        return analysesToday < this.properties.getMaxAgentAnalysesPerDay();
    }
}
