package com.sitionix.atmssox.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectFlow {

    UUID projectId;

    UUID flowId;

    List<AgentProjectFlowNode> nodes;

    List<AgentProjectFlowEdge> edges;
}
