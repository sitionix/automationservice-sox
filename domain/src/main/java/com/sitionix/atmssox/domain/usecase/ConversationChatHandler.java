package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import java.util.List;

public interface ConversationChatHandler {

    ChatAgentResponse handle(Conversation conversation,
                             List<ConversationParticipant> participants,
                             ChatAgentCommand command,
                             Long userId);
}
