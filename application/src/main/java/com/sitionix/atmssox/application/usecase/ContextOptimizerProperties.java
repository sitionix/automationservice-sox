package com.sitionix.atmssox.application.usecase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "automation.context-optimizer")
public class ContextOptimizerProperties {

    private boolean enabled = true;

    private int lastMessagesLimit = 10;

    private int optimizeThresholdMessages = 20;

    private int conversationCooldownMinutes = 15;

    private int maxSummaryLength = 2000;
}
