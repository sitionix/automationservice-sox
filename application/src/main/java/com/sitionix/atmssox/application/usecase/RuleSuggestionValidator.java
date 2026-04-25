package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.AgentRule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RuleSuggestionValidator {

    private static final Set<String> GENERIC_RULES = Set.of(
            "be helpful",
            "be clear"
    );

    private final RuleSuggestionAnalyzerProperties properties;

    public List<RuleSuggestionCandidate> validate(final List<RuleSuggestionCandidate> suggestions,
                                                  final List<AgentRule> activeRules,
                                                  final List<AgentRule> pendingRules) {
        final int maxSuggestionsPerRun = Math.max(0, this.properties.getMaxSuggestionsPerRun());
        final int remainingPendingSlots = Math.max(0, this.properties.getMaxPendingSuggestionsPerAgent() - pendingRules.size());
        final int allowedSuggestions = Math.min(maxSuggestionsPerRun, remainingPendingSlots);
        if (allowedSuggestions == 0 || suggestions.isEmpty()) {
            return List.of();
        }

        final Set<String> existingRuleContents = new HashSet<>();
        activeRules.stream()
                .map(AgentRule::getContent)
                .map(this::normalize)
                .forEach(existingRuleContents::add);
        pendingRules.stream()
                .map(AgentRule::getContent)
                .map(this::normalize)
                .forEach(existingRuleContents::add);

        final List<RuleSuggestionCandidate> valid = new ArrayList<>();
        for (final RuleSuggestionCandidate suggestion : suggestions) {
            if (valid.size() >= allowedSuggestions) {
                break;
            }
            final RuleSuggestionCandidate normalizedSuggestion = this.normalizeSuggestion(suggestion);
            if (!this.isValid(normalizedSuggestion)) {
                continue;
            }
            final String normalizedContent = this.normalize(normalizedSuggestion.content());
            if (existingRuleContents.contains(normalizedContent)) {
                continue;
            }
            existingRuleContents.add(normalizedContent);
            valid.add(normalizedSuggestion);
        }
        return valid;
    }

    private RuleSuggestionCandidate normalizeSuggestion(final RuleSuggestionCandidate suggestion) {
        if (suggestion == null) {
            return new RuleSuggestionCandidate(null, null, null);
        }
        final String title = suggestion.title() == null ? null : suggestion.title().trim();
        final String content = suggestion.content() == null ? null : suggestion.content().trim();
        final String reason = suggestion.reason() == null ? null : suggestion.reason().trim();
        return new RuleSuggestionCandidate(title, content, reason);
    }

    private boolean isValid(final RuleSuggestionCandidate suggestion) {
        if (!StringUtils.hasText(suggestion.title())
                || !StringUtils.hasText(suggestion.content())
                || !StringUtils.hasText(suggestion.reason())) {
            return false;
        }
        if (suggestion.content().length() > this.properties.getMaxSuggestionContentLength()) {
            return false;
        }
        final String normalizedContent = this.normalize(suggestion.content());
        return !GENERIC_RULES.contains(normalizedContent);
    }

    private String normalize(final String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value
                .trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[.!?]+$", "")
                .toLowerCase(Locale.ROOT);
    }
}
