package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConversationMessageWindowService {

    public List<ConversationMessage> takeLastMessages(final List<ConversationMessage> history, final int limit) {
        if (history.isEmpty()) {
            return List.of();
        }
        final int normalizedLimit = Math.max(1, limit);
        final int fromIndex = Math.max(0, history.size() - normalizedLimit);
        return history.subList(fromIndex, history.size());
    }

    public Optional<String> findLatestUserMessageContent(final List<ConversationMessage> history) {
        return history.stream()
                .filter(message -> message.getAuthorType() == ConversationParticipantType.USER)
                .reduce((first, second) -> second)
                .map(ConversationMessage::getContent)
                .map(AgentRuleTextNormalizer::normalizeToEmpty)
                .filter(content -> !content.isEmpty());
    }
}
