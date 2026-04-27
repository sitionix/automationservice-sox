package com.sitionix.atmssox.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "automation.async")
public class AsyncExecutorsProperties {

    private ExecutorProperties ruleSuggestionAnalyzer = new ExecutorProperties();

    private ExecutorProperties contextOptimizer = new ExecutorProperties();

    @Getter
    @Setter
    public static class ExecutorProperties {

        private String threadNamePrefix = "executor-";

        private int corePoolSize = 2;

        private int maxPoolSize = 4;

        private int queueCapacity = 100;
    }
}
