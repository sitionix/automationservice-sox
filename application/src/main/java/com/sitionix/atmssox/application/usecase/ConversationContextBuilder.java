package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationAuthorType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConversationContextBuilder {

    public String build(final List<ConversationMessage> orderedHistory) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Conversation history:\n");
        for (ConversationMessage message : orderedHistory) {
            if (message.getAuthorType() == ConversationAuthorType.USER) {
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
