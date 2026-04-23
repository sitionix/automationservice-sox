package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.AgentRule;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConversationContextBuilder {

    public String build(final List<AgentRule> activeRules, final List<ConversationMessage> orderedHistory) {
        final StringBuilder builder = new StringBuilder();
        if (!activeRules.isEmpty()) {
            builder.append("Active rules:\n");
            for (AgentRule rule : activeRules) {
                builder.append("- ").append(rule.getText()).append("\n");
            }
            builder.append("\n");
        }
        builder.append("Conversation history:\n");
        for (ConversationMessage message : orderedHistory) {
            if (message.getAuthorType() == ConversationParticipantType.USER) {
                builder.append("USER: ");
            } else {
                builder.append("AGENT: ");
            }
            builder.append(message.getContent()).append("\n");
        }
        builder.append("Respond as AGENT to the latest USER message.");
        return builder.toString();
    }
}
