package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("multiAgentConversationChatHandler")
@RequiredArgsConstructor
public class MultiAgentConversationChatHandler implements ConversationChatHandler {

    @Override
    public ChatAgentResponse handle(final Conversation conversation,
                                    final List<ConversationParticipant> participants,
                                    final ChatAgentCommand command,
                                    final Long userId) {
        throw new AgentChatNotAllowedException("Messaging for project conversations is not available yet");
    }
}
