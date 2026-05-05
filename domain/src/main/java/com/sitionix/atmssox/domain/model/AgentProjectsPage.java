package com.sitionix.atmssox.domain.model;

import java.util.List;
import lombok.Builder;

@Builder
public record AgentProjectsPage(
        List<AgentProject> items,
        int page,
        int size,
        boolean hasNext
) {
}
