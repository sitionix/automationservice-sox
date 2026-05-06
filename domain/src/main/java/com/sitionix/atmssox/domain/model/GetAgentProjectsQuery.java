package com.sitionix.atmssox.domain.model;

import lombok.Builder;

@Builder
public record GetAgentProjectsQuery(
        Integer page,
        Integer size
) {
}
