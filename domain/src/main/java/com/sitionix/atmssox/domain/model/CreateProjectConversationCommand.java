package com.sitionix.atmssox.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CreateProjectConversationCommand {

    List<UUID> agentIds;
}
