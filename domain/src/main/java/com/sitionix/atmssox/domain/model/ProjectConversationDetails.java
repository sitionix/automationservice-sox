package com.sitionix.atmssox.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ProjectConversationDetails {

    Conversation conversation;

    AgentProject project;

    List<ConversationParticipant> participants;

    List<ConversationMessage> messages;

    public List<ConversationParticipant> getAgentParticipants() {
        if (this.participants == null) {
            return List.of();
        }
        return this.participants.stream()
                .filter(participant -> participant.getParticipantType() == ConversationParticipantType.AGENT)
                .toList();
    }

    public List<ConversationMessage> getMessages() {
        return this.messages == null ? List.of() : this.messages;
    }
}
