package com.sitionix.atmssox.application.usecase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "automation.rule-suggestion-analyzer")
public class RuleSuggestionAnalyzerProperties {

    private boolean enabled = true;

    private int lastMessagesLimit = 20;

    private int messageCountThreshold = 10;

    private int conversationCooldownMinutes = 30;

    private int maxAgentAnalysesPerDay = 3;

    private int maxPendingSuggestionsPerAgent = 5;

    private int maxSuggestionsPerRun = 3;

    private int maxSuggestionContentLength = 1000;
}
