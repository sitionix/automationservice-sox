package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private final SystemAgentExecutor systemAgentExecutor;
    private final RuleSuggestionAnalyzerProperties properties;
    private final ObjectMapper objectMapper;

    @Async("ruleSuggestionAnalyzerTaskExecutor")
    public void analyzeAsync(final UUID agentId, final UUID conversationId) {
        final Optional<Agent> analyzerOptional = this.agentRepository.findSystemRuleAnalyzer();
        if (analyzerOptional.isEmpty() || analyzerOptional.get().getStatus() != AgentStatus.ACTIVE) {
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

        final List<ConversationMessage> fullHistory =
                this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        final String latestUserMessage = fullHistory.stream()
                .filter(message -> message.getAuthorType() == ConversationParticipantType.USER)
                .reduce((first, second) -> second)
                .map(ConversationMessage::getContent)
                .map(this::normalize)
                .orElse("");
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
        final RuleSuggestionAnalysisContext context = new RuleSuggestionAnalysisContext(
                targetAgent,
                activeRules,
                pendingRules,
                this.takeLastMessages(fullHistory),
                latestUserMessage
        );

        final String rawResponse;
        try {
            rawResponse = this.systemAgentExecutor.execute(analyzerOptional.get(), context);
        } catch (OpenAiExecutionException exception) {
            log.warn("Rule suggestion analyzer OpenAI execution failed for agentId={}, conversationId={}", agentId, conversationId, exception);
            return;
        }
        final List<RuleSuggestionCandidate> suggestions = this.parseSuggestions(rawResponse, agentId, conversationId);
        final List<RuleSuggestionCandidate> validSuggestions = this.filterValidSuggestions(suggestions, activeRules, pendingRules);
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

    private List<ConversationMessage> takeLastMessages(final List<ConversationMessage> fullHistory) {
        if (fullHistory.isEmpty()) {
            return List.of();
        }
        final int fromIndex = Math.max(0, fullHistory.size() - Math.max(1, this.properties.getLastMessagesLimit()));
        return fullHistory.subList(fromIndex, fullHistory.size());
    }

    private List<RuleSuggestionCandidate> parseSuggestions(final String rawResponse,
                                                           final UUID agentId,
                                                           final UUID conversationId) {
        try {
            final JsonNode root = this.objectMapper.readTree(rawResponse);
            final JsonNode suggestions = root.path("suggestions");
            if (!suggestions.isArray()) {
                return List.of();
            }
            final List<RuleSuggestionDto> parsed = this.objectMapper.convertValue(
                    suggestions,
                    new TypeReference<List<RuleSuggestionDto>>() {
                    }
            );
            return parsed.stream()
                    .map(item -> new RuleSuggestionCandidate(item.title(), item.content(), item.reason()))
                    .toList();
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            log.warn("Rule suggestion analyzer returned invalid JSON for agentId={}, conversationId={}", agentId, conversationId, exception);
            return List.of();
        }
    }

    private List<RuleSuggestionCandidate> filterValidSuggestions(final List<RuleSuggestionCandidate> suggestions,
                                                                 final List<AgentRule> activeRules,
                                                                 final List<AgentRule> pendingRules) {
        final Set<String> existingContents = new LinkedHashSet<>();
        existingContents.addAll(activeRules.stream().map(AgentRule::getContent).map(this::normalizeContent).toList());
        existingContents.addAll(pendingRules.stream().map(AgentRule::getContent).map(this::normalizeContent).toList());

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
                this.normalize(suggestion == null ? null : suggestion.title()),
                this.normalize(suggestion == null ? null : suggestion.content()),
                this.normalize(suggestion == null ? null : suggestion.reason())
        );
    }

    private String normalizeContent(final String content) {
        return this.normalize(content)
                .toLowerCase()
                .replaceAll("\\s+", " ");
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }

    private record RuleSuggestionDto(String title, String content, String reason) {
    }
}
