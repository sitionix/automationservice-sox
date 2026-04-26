package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;
import java.util.List;

public record ContextOptimizationContext(
        Agent targetAgent,
        String existingSummary,
        List<ConversationMessage> messagesToSummarize
) implements AgentExecutionContext {
}
