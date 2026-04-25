package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.List;

public record RuleSuggestionAnalysisContext(
        Agent targetAgent,
        List<AgentRule> activeRules,
        List<AgentRule> pendingRules,
        List<ConversationMessage> messages,
        String latestUserMessage
) implements SystemAgentContext {
}
