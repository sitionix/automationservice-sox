package com.sitionix.atmssox.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RuleSuggestionAnalysisRun {

    UUID id;

    UUID agentId;

    UUID conversationId;

    long userMessageCount;

    Instant createdAt;
}
