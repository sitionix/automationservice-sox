package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleSuggestionAnalyzerPromptBuilder implements SystemAgentPromptBuilder {

    @Override
    public AgentType getAgentType() {
        return AgentType.SYSTEM_RULE_ANALYZER;
    }

    @Override
    public String build(final SystemAgentContext context) {
        if (!(context instanceof RuleSuggestionAnalysisContext analysisContext)) {
            throw new AgentValidationException("RuleSuggestionAnalysisContext is required");
        }
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
                this.normalize(analysisContext.targetAgent().getInstruction()),
                this.formatRules(analysisContext.activeRules()),
                this.formatRules(analysisContext.pendingRules()),
                this.formatMessages(analysisContext.messages()),
                this.normalize(analysisContext.latestUserMessage())
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

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
