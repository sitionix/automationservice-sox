package com.sitionix.atmssox.application.usecase;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatExecutionAsyncRunner {

    private final ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @Async("contextOptimizerTaskExecutor")
    @Transactional
    public void processAsync(final UUID executionId) {
        log.info("[CHAT_EXECUTION] async started executionId={}", executionId);
        this.chatExecutionAsyncProcessor.process(executionId);
    }
}
