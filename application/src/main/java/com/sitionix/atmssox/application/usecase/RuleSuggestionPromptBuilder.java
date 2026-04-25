package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RuleSuggestionPromptBuilder {

    public String build(final Agent targetAgent,
                        final List<AgentRule> activeRules,
                        final List<AgentRule> pendingRules,
                        final List<ConversationMessage> lastMessages,
                        final String latestUserMessage) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Target USER agent instruction:\n");
        builder.append(targetAgent.getInstruction() == null ? "" : targetAgent.getInstruction().trim()).append("\n\n");

        builder.append("ACTIVE rules:\n");
        if (activeRules.isEmpty()) {
            builder.append("- none\n");
        } else {
            for (final AgentRule rule : activeRules) {
                builder.append("- ").append(rule.getTitle()).append(": ").append(rule.getContent()).append("\n");
            }
        }
        builder.append("\n");

        builder.append("PENDING rules:\n");
        if (pendingRules.isEmpty()) {
            builder.append("- none\n");
        } else {
            for (final AgentRule rule : pendingRules) {
                builder.append("- ").append(rule.getTitle()).append(": ").append(rule.getContent()).append("\n");
            }
        }
        builder.append("\n");

        builder.append("Last conversation messages:\n");
        for (final ConversationMessage message : lastMessages) {
            final String role = message.getAuthorType() == ConversationParticipantType.USER ? "USER" : "AGENT";
            builder.append(role).append(": ").append(message.getContent()).append("\n");
        }
        builder.append("\n");

        builder.append("Latest user message:\n");
        builder.append(latestUserMessage == null ? "" : latestUserMessage.trim()).append("\n");
        return builder.toString();
    }
}
