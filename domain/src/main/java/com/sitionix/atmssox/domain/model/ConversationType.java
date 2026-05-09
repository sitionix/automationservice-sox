package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public enum ConversationType {
    DIRECT("directConversationChatHandler"),
    MULTI_AGENT("multiAgentConversationChatHandler");

    @Getter
    private final String bindingKey;

    @Setter
    @Getter
    private ConversationChatHandler handler;

    public ChatAgentResponse handle(final Conversation conversation,
                                    final List<ConversationParticipant> participants,
                                    final ChatAgentCommand command,
                                    final Long userId) {
        if (this.handler == null) {
            throw new IllegalStateException("No handler configured for conversation type: " + this.name());
        }
        return this.handler.handle(conversation, participants, command, userId);
    }

    public static ConversationType byBindingKey(final String bindingKey) {
        return Arrays.stream(values())
                .filter(conversationType -> conversationType.bindingKey.equals(bindingKey))
                .findFirst()
                .orElse(null);
    }
}
