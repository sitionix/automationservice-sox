package com.sitionix.atmssox.config;

import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfiguration {

    private final AsyncExecutorsProperties asyncExecutorsProperties;

    @Bean(name = "ruleSuggestionAnalyzerTaskExecutor")
    public Executor ruleSuggestionAnalyzerTaskExecutor() {
        return this.buildExecutor(this.asyncExecutorsProperties.getRuleSuggestionAnalyzer());
    }

    @Bean(name = "contextOptimizerTaskExecutor")
    public Executor contextOptimizerTaskExecutor() {
        return this.buildExecutor(this.asyncExecutorsProperties.getContextOptimizer());
    }

    private Executor buildExecutor(final AsyncExecutorsProperties.ExecutorProperties properties) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.initialize();
        return executor;
    }
}
