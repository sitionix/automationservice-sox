package com.sitionix.atmssox.domain.model;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectFlowNode {

    UUID id;

    String nodeType;

    UUID referenceId;

    Double positionX;

    Double positionY;

    String designStatus;

    Map<String, Object> config;
}
