package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
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
public class RuleSuggestionAnalyzerAsyncService {

    private static final String DEFAULT_ANALYZER_INSTRUCTION = """
            You analyze agent conversations and suggest rules that improve behavior.

            Rules must:
            - reflect repeated user preferences or corrections
            - be specific and actionable
            - not be generic ("be helpful")

            Return ONLY valid JSON in this format:

            {
              "suggestions": [
                {
                  "title": "...",
                  "content": "...",
                  "reason": "..."
                }
              ]
            }
            """;

    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final RuleSuggestionAnalysisRunRepository ruleSuggestionAnalysisRunRepository;
    private final OpenAiChatClient openAiChatClient;
    private final RuleSuggestionAnalyzerProperties properties;
    private final RuleSuggestionPromptBuilder ruleSuggestionPromptBuilder;
    private final RuleSuggestionResponseParser ruleSuggestionResponseParser;
    private final RuleSuggestionValidator ruleSuggestionValidator;

    @Async("ruleSuggestionAnalyzerTaskExecutor")
    public void analyzeAsync(final UUID agentId, final UUID conversationId) {
        long userMessageCount = 0;
        boolean shouldPersistRun = false;
        try {
            final Optional<Agent> analyzer = this.agentRepository.findSystemRuleAnalyzer();
            if (analyzer.isEmpty() || analyzer.get().getStatus() != AgentStatus.ACTIVE) {
                log.warn("Rule analyzer agent is missing or inactive");
                return;
            }

            final Optional<Agent> targetAgentOptional = this.agentRepository.findById(agentId);
            if (targetAgentOptional.isEmpty()) {
                return;
            }
            final Agent targetAgent = targetAgentOptional.get();
            if (targetAgent.getType() != AgentType.USER || targetAgent.getStatus() != AgentStatus.ACTIVE) {
                return;
            }
            shouldPersistRun = true;

            userMessageCount = this.conversationMessageRepository.countByConversationIdAndAuthorType(
                    conversationId,
                    ConversationParticipantType.USER
            );
            final List<ConversationMessage> fullHistory = this.conversationMessageRepository
                    .findAllByConversationIdOrderByCreatedAtAsc(conversationId);
            final Optional<ConversationMessage> latestUserMessage = fullHistory.stream()
                    .filter(message -> message.getAuthorType() == ConversationParticipantType.USER)
                    .reduce((first, second) -> second);
            if (latestUserMessage.isEmpty()) {
                return;
            }

            final List<AgentRule> activeRules = this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                    agentId,
                    targetAgent.getUserId(),
                    AgentRuleStatus.ACTIVE,
                    null
            );
            final List<AgentRule> pendingRules = this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                    agentId,
                    targetAgent.getUserId(),
                    AgentRuleStatus.PENDING,
                    null
            );

            final String analyzerInstruction = this.normalizeAnalyzerInstruction(analyzer.get().getInstruction());
            final String prompt = this.ruleSuggestionPromptBuilder.build(
                    targetAgent,
                    activeRules,
                    pendingRules,
                    this.takeLastMessages(fullHistory),
                    latestUserMessage.get().getContent()
            );
            final String rawResponse = this.openAiChatClient.execute(analyzerInstruction, prompt);
            final List<RuleSuggestionCandidate> parsed = this.ruleSuggestionResponseParser.parse(rawResponse);
            final List<RuleSuggestionCandidate> validSuggestions = this.ruleSuggestionValidator.validate(parsed, activeRules, pendingRules);

            if (validSuggestions.isEmpty()) {
                return;
            }

            final Instant now = Instant.now();
            for (final RuleSuggestionCandidate suggestion : validSuggestions) {
                this.agentRuleRepository.save(AgentRule.builder()
                        .id(UUID.randomUUID())
                        .agentId(agentId)
                        .title(suggestion.title())
                        .content(suggestion.content())
                        .status(AgentRuleStatus.PENDING)
                        .authorType(AgentRuleAuthorType.AI)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
            }
        } catch (Exception exception) {
            log.warn("Rule suggestion analyzer failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        } finally {
            if (shouldPersistRun) {
                this.persistRun(agentId, conversationId, userMessageCount);
            }
        }
    }

    private List<ConversationMessage> takeLastMessages(final List<ConversationMessage> fullHistory) {
        if (fullHistory.isEmpty()) {
            return List.of();
        }
        final int lastMessagesLimit = Math.max(1, this.properties.getLastMessagesLimit());
        final int fromIndex = Math.max(0, fullHistory.size() - lastMessagesLimit);
        return fullHistory.subList(fromIndex, fullHistory.size());
    }

    private String normalizeAnalyzerInstruction(final String instruction) {
        if (instruction == null || instruction.trim().isEmpty()) {
            return DEFAULT_ANALYZER_INSTRUCTION;
        }
        return instruction.trim();
    }

    private void persistRun(final UUID agentId, final UUID conversationId, final long userMessageCount) {
        try {
            this.ruleSuggestionAnalysisRunRepository.save(RuleSuggestionAnalysisRun.builder()
                    .id(UUID.randomUUID())
                    .agentId(agentId)
                    .conversationId(conversationId)
                    .userMessageCount(userMessageCount)
                    .createdAt(Instant.now())
                    .build());
        } catch (Exception exception) {
            log.warn("Failed to persist analyzer execution history for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }
}
