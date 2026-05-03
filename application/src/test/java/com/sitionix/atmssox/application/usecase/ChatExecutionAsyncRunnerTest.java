package com.sitionix.atmssox.application.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class ChatExecutionAsyncRunnerTest {

    @Mock
    private ObjectProvider<ChatExecutionAsyncProcessor> chatExecutionAsyncProcessorProvider;

    private ChatExecutionAsyncRunner chatExecutionAsyncRunner;

    @BeforeEach
    void setUp() {
        this.chatExecutionAsyncRunner = new ChatExecutionAsyncRunner(this.chatExecutionAsyncProcessorProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.chatExecutionAsyncProcessorProvider);
    }

    @Test
    void givenExecutionId_whenProcessAsync_thenDelegateToProcessor() {
        //given
        final UUID executionId = UUID.randomUUID();
        final ChatExecutionAsyncProcessor chatExecutionAsyncProcessor = mock(ChatExecutionAsyncProcessor.class);
        when(this.chatExecutionAsyncProcessorProvider.getObject()).thenReturn(chatExecutionAsyncProcessor);

        //when
        this.chatExecutionAsyncRunner.processAsync(executionId);

        //then
        verify(this.chatExecutionAsyncProcessorProvider).getObject();
        verify(chatExecutionAsyncProcessor).process(executionId);
        verifyNoMoreInteractions(chatExecutionAsyncProcessor);
    }
}
