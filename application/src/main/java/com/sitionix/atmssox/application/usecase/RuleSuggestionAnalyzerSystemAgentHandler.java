package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleSuggestionAnalyzerSystemAgentHandler implements SystemAgentHandler {

    private final OpenAiChatClient openAiChatClient;

    public RuleSuggestionAnalyzerSystemAgentHandler(final OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    @Override
    public String execute(final Agent agent, final SystemAgentContext context) {
        if (!(context instanceof RuleSuggestionAnalysisContext analysisContext)) {
            throw new AgentValidationException("RuleSuggestionAnalysisContext is required");
        }

        final String instruction = this.normalize(agent.getInstruction());
        if (instruction.isEmpty()) {
            throw new AgentValidationException("System agent instruction is empty");
        }

        return this.openAiChatClient.execute(instruction, this.buildPrompt(analysisContext));
    }

    private String buildPrompt(final RuleSuggestionAnalysisContext analysisContext) {
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
