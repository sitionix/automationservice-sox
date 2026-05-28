package com.sitionix.atmssox.domain.model;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectFlowEdge {

    UUID id;

    UUID sourceNodeId;

    UUID targetNodeId;

    String edgeType;

    Map<String, Object> config;
}
