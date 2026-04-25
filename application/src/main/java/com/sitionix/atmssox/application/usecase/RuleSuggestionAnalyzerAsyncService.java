package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
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

    private static final Set<String> GENERIC_RULES = Set.of("be helpful", "be clear");

    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final OpenAiChatClient openAiChatClient;
    private final RuleSuggestionAnalyzerProperties properties;
    private final ObjectMapper objectMapper;

    @Async("ruleSuggestionAnalyzerTaskExecutor")
    public void analyzeAsync(final UUID agentId, final UUID conversationId) {
        try {
            final Optional<Agent> analyzerOptional = this.agentRepository.findSystemRuleAnalyzer();
            if (analyzerOptional.isEmpty() || analyzerOptional.get().getStatus() != AgentStatus.ACTIVE) {
                return;
            }
            final String analyzerInstruction = this.normalize(analyzerOptional.get().getInstruction());
            if (analyzerInstruction.isEmpty()) {
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

            final String prompt = this.buildPrompt(targetAgent, activeRules, pendingRules, this.takeLastMessages(fullHistory), latestUserMessage);
            final String rawResponse = this.openAiChatClient.execute(analyzerInstruction, prompt);
            final List<RuleSuggestionCandidate> suggestions = this.parseSuggestions(rawResponse);
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
        } catch (Exception exception) {
            log.warn("Rule suggestion analyzer failed for agentId={}, conversationId={}", agentId, conversationId, exception);
        }
    }

    private String buildPrompt(final Agent targetAgent,
                               final List<AgentRule> activeRules,
                               final List<AgentRule> pendingRules,
                               final List<ConversationMessage> messages,
                               final String latestUserMessage) {
        return """
                Analyze conversation and propose agent rules.
                Return only JSON:
                {"suggestions":[{"title":"...","content":"...","reason":"..."}]}

                Agent instruction:
                %s

                Active rules:
                %s

                Pending rules:
                %s

                Conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                this.normalize(targetAgent.getInstruction()),
                this.formatRules(activeRules),
                this.formatRules(pendingRules),
                this.formatMessages(messages),
                latestUserMessage
        );
    }

    private String formatRules(final List<AgentRule> rules) {
        if (rules.isEmpty()) {
            return "(none)";
        }
        return rules.stream()
                .map(rule -> "- " + this.normalize(rule.getTitle()) + ": " + this.normalize(rule.getContent()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse("(none)");
    }

    private String formatMessages(final List<ConversationMessage> messages) {
        if (messages.isEmpty()) {
            return "(none)";
        }
        return messages.stream()
                .map(message -> message.getAuthorType().name() + ": " + this.normalize(message.getContent()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse("(none)");
    }

    private List<ConversationMessage> takeLastMessages(final List<ConversationMessage> fullHistory) {
        if (fullHistory.isEmpty()) {
            return List.of();
        }
        final int fromIndex = Math.max(0, fullHistory.size() - Math.max(1, this.properties.getLastMessagesLimit()));
        return fullHistory.subList(fromIndex, fullHistory.size());
    }

    private List<RuleSuggestionCandidate> parseSuggestions(final String rawResponse) {
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
        } catch (Exception exception) {
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
                .filter(candidate -> !GENERIC_RULES.contains(candidate.content().toLowerCase()))
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
