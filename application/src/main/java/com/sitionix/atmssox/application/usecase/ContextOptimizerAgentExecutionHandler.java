package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContextOptimizerAgentExecutionHandler implements AgentExecutionHandler<ContextOptimizationContext> {

    private static final String NO_DATA = "(none)";

    private final OpenAiChatClient openAiChatClient;

    @Override
    public Class<ContextOptimizationContext> supportedContextType() {
        return ContextOptimizationContext.class;
    }

    @Override
    public String execute(final Agent agent, final ContextOptimizationContext context) {
        final String instruction = AgentRuleTextNormalizer.normalizeToEmpty(agent.getInstruction());
        if (instruction.isEmpty()) {
            throw new AgentValidationException("System agent instruction is empty");
        }

        return this.openAiChatClient.execute(new OpenAiChatRequest(instruction, this.buildPrompt(context)));
    }

    private String buildPrompt(final ContextOptimizationContext context) {
        return """
                Target agent instruction:
                %s

                Existing summary:
                %s

                Messages to summarize:
                %s
                """.formatted(
                AgentRuleTextNormalizer.normalizeToEmpty(context.targetAgentInstruction()),
                this.formatExistingSummary(context.existingSummary()),
                this.formatMessages(context.messagesToSummarize())
        );
    }

    private String formatExistingSummary(final String existingSummary) {
        final String normalizedSummary = AgentRuleTextNormalizer.normalizeToEmpty(existingSummary);
        return normalizedSummary.isEmpty() ? NO_DATA : normalizedSummary;
    }

    private String formatMessages(final List<ConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return NO_DATA;
        }
        return messages.stream()
                .map(message -> message.getAuthorType().name() + ": "
                        + AgentRuleTextNormalizer.normalizeToEmpty(message.getContent()))
                .reduce((first, second) -> first + "\n" + second)
                .orElse(NO_DATA);
    }
}
