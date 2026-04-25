package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleSuggestionAnalyzerAgentExecutionHandler implements AgentExecutionHandler<RuleSuggestionAnalysisContext> {

    private final OpenAiChatClient openAiChatClient;

    @Override
    public Class<RuleSuggestionAnalysisContext> supportedContextType() {
        return RuleSuggestionAnalysisContext.class;
    }

    @Override
    public String execute(final Agent agent, final RuleSuggestionAnalysisContext analysisContext) {
        final String instruction = AgentRuleTextNormalizer.normalizeToEmpty(agent.getInstruction());
        if (instruction.isEmpty()) {
            throw new AgentValidationException("System agent instruction is empty");
        }

        return this.openAiChatClient.execute(new OpenAiChatRequest(instruction, this.buildPrompt(analysisContext)));
    }

    private String buildPrompt(final RuleSuggestionAnalysisContext analysisContext) {
        return """
                Target agent instruction:
                %s

                Active rules:
                %s

                Pending rules:
                %s

                Rejected rules:
                %s

                Conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                AgentRuleTextNormalizer.normalizeToEmpty(analysisContext.targetAgent().getInstruction()),
                this.formatRules(analysisContext.activeRules()),
                this.formatRules(analysisContext.pendingRules()),
                this.formatRules(analysisContext.rejectedRules()),
                this.formatMessages(analysisContext.messages()),
                AgentRuleTextNormalizer.normalizeToEmpty(analysisContext.latestUserMessage())
        );
    }

    private String formatRules(final List<AgentRule> rules) {
        if (rules.isEmpty()) {
            return "(none)";
        }
        return rules.stream()
                .map(rule -> "- " + AgentRuleTextNormalizer.normalizeToEmpty(rule.getTitle()) + ": "
                        + AgentRuleTextNormalizer.normalizeToEmpty(rule.getContent()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse("(none)");
    }

    private String formatMessages(final List<ConversationMessage> messages) {
        if (messages.isEmpty()) {
            return "(none)";
        }
        return messages.stream()
                .map(message -> message.getAuthorType().name() + ": " + AgentRuleTextNormalizer.normalizeToEmpty(message.getContent()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse("(none)");
    }
}
