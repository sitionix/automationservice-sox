package com.sitionix.atmssox.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AgentProjectFlowPaletteSource {

    String sourceType;

    UUID sourceId;

    String sourceName;
}
