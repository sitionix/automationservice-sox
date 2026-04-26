package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleSuggestionAnalyzerAsyncService {

    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AgentExecutionService agentExecutionService;
    private final RuleSuggestionAnalyzerProperties properties;
    private final OpenAiJsonResponseParser openAiJsonResponseParser;
    private final ActiveUserAgentResolver activeUserAgentResolver;
    private final ConversationMessageWindowService conversationMessageWindowService;

    @Async("ruleSuggestionAnalyzerTaskExecutor")
    public void analyzeAsync(final UUID agentId, final UUID conversationId) {
        final Optional<Agent> analyzerOptional = this.findActiveAnalyzer();
        if (analyzerOptional.isEmpty()) {
            return;
        }
        final Agent analyzer = analyzerOptional.get();

        final Optional<Agent> targetAgentOptional = this.activeUserAgentResolver.findById(agentId);
        if (targetAgentOptional.isEmpty()) {
            return;
        }
        final Agent targetAgent = targetAgentOptional.get();

        final List<ConversationMessage> fullHistory =
                this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        final Optional<String> latestUserMessage = this.conversationMessageWindowService.findLatestUserMessageContent(fullHistory);
        if (latestUserMessage.isEmpty()) {
            return;
        }

        final List<AgentRule> activeRules = this.findRules(agentId, targetAgent.getUserId(), AgentRuleStatus.ACTIVE);
        final List<AgentRule> pendingRules = this.findRules(agentId, targetAgent.getUserId(), AgentRuleStatus.PENDING);
        final List<AgentRule> rejectedRules = this.findRules(agentId, targetAgent.getUserId(), AgentRuleStatus.REJECTED);
        final RuleSuggestionAnalysisContext context = new RuleSuggestionAnalysisContext(
                targetAgent,
                activeRules,
                pendingRules,
                rejectedRules,
                this.conversationMessageWindowService.takeLastMessages(fullHistory, this.properties.getLastMessagesLimit()),
                latestUserMessage.get()
        );
        final Optional<String> rawResponse = this.executeAnalyzer(analyzer, context, agentId, conversationId);
        if (rawResponse.isEmpty()) {
            return;
        }
        final List<RuleSuggestionCandidate> suggestions = this.parseSuggestions(rawResponse.get(), agentId, conversationId);
        final List<RuleSuggestionCandidate> validSuggestions = this.filterValidSuggestions(suggestions, activeRules, pendingRules, rejectedRules);
        if (validSuggestions.isEmpty()) {
            return;
        }

        final Instant now = Instant.now();
        validSuggestions.stream()
                .map(suggestion -> AgentRule.builder()
                        .id(UUID.randomUUID())
                        .agentId(agentId)
                        .title(suggestion.title())
                        .content(suggestion.content())
                        .status(AgentRuleStatus.PENDING)
                        .authorType(AgentRuleAuthorType.AI)
                        .createdAt(now)
                        .updatedAt(now)
                        .build())
                .forEach(this.agentRuleRepository::save);
    }

    private Optional<Agent> findActiveAnalyzer() {
        return this.agentRepository.findSystemRuleAnalyzer()
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE);
    }

    private List<AgentRule> findRules(final UUID agentId, final Long userId, final AgentRuleStatus status) {
        return this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                agentId,
                userId,
                status,
                null
        );
    }

    private Optional<String> executeAnalyzer(final Agent analyzer,
                                             final RuleSuggestionAnalysisContext context,
                                             final UUID agentId,
                                             final UUID conversationId) {
        try {
            return Optional.of(this.agentExecutionService.execute(analyzer, context));
        } catch (OpenAiExecutionException exception) {
            log.warn("Rule suggestion analyzer OpenAI execution failed for agentId={}, conversationId={}", agentId, conversationId, exception);
            return Optional.empty();
        }
    }

    private List<RuleSuggestionCandidate> parseSuggestions(final String rawResponse,
                                                           final UUID agentId,
                                                           final UUID conversationId) {
        final Optional<JsonNode> root = this.openAiJsonResponseParser.parseObject(rawResponse);
        if (root.isEmpty()) {
            log.warn("Rule suggestion analyzer returned invalid JSON for agentId={}, conversationId={}", agentId, conversationId);
            return List.of();
        }
        final List<RuleSuggestionDto> parsed = this.openAiJsonResponseParser.parseArrayField(
                root.get(),
                "suggestions",
                new TypeReference<List<RuleSuggestionDto>>() {
                }
        );
        try {
            return parsed.stream()
                    .map(item -> new RuleSuggestionCandidate(item.title(), item.content(), item.reason()))
                    .toList();
        } catch (RuntimeException exception) {
            log.warn("Rule suggestion analyzer returned invalid JSON for agentId={}, conversationId={}", agentId, conversationId, exception);
            return List.of();
        }
    }

    private List<RuleSuggestionCandidate> filterValidSuggestions(final List<RuleSuggestionCandidate> suggestions,
                                                                 final List<AgentRule> activeRules,
                                                                 final List<AgentRule> pendingRules,
                                                                 final List<AgentRule> rejectedRules) {
        final Set<String> existingContents = Stream.of(activeRules, pendingRules, rejectedRules)
                .flatMap(List::stream)
                .map(AgentRule::getContent)
                .map(this::normalizeContent)
                .collect(Collectors.toSet());

        return suggestions.stream()
                .map(this::normalizeCandidate)
                .filter(candidate -> !candidate.title().isEmpty())
                .filter(candidate -> !candidate.content().isEmpty())
                .filter(candidate -> !candidate.reason().isEmpty())
                .filter(candidate -> candidate.content().length() <= this.properties.getMaxSuggestionContentLength())
                .filter(candidate -> !existingContents.contains(this.normalizeContent(candidate.content())))
                .limit(this.properties.getMaxSuggestionsPerRun())
                .toList();
    }

    private RuleSuggestionCandidate normalizeCandidate(final RuleSuggestionCandidate suggestion) {
        return new RuleSuggestionCandidate(
                AgentRuleTextNormalizer.normalizeToEmpty(suggestion == null ? null : suggestion.title()),
                AgentRuleTextNormalizer.normalizeToEmpty(suggestion == null ? null : suggestion.content()),
                AgentRuleTextNormalizer.normalizeToEmpty(suggestion == null ? null : suggestion.reason())
        );
    }

    private String normalizeContent(final String content) {
        return AgentRuleTextNormalizer.normalizeToEmpty(content)
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    private record RuleSuggestionDto(String title, String content, String reason) {
    }
}
