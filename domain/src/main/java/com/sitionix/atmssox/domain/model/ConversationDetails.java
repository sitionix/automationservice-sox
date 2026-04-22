package com.sitionix.atmssox.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ConversationDetails {

    Conversation conversation;

    List<ConversationMessage> messages;
}
