package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;
import java.util.List;

public record ContextOptimizationContext(
        String targetAgentInstruction,
        String existingSummary,
        List<ConversationMessage> messagesToSummarize
) implements AgentExecutionContext {
}
