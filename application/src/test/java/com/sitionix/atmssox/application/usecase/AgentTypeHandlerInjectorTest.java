package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentTypeHandlerInjectorTest {

    private AgentTypeHandlerInjector agentTypeHandlerInjector;

    @Mock
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        this.agentTypeHandlerInjector = new AgentTypeHandlerInjector(this.context);
    }

    @AfterEach
    void tearDown() {
        AgentType.USER.setHandler(null);
        AgentType.SYSTEM_RULE_ANALYZER.setHandler(null);
        AgentType.SYSTEM_CONTEXT_OPTIMIZER.setHandler(null);
        verifyNoMoreInteractions(this.context);
    }

    @Test
    void givenHandlerBeanMap_whenInjectHandlers_thenAssignHandlersToAgentTypes() {
        //given
        final AgentExecutionHandler<?> userHandler = mock(AgentExecutionHandler.class);
        final AgentExecutionHandler<?> analyzerHandler = mock(AgentExecutionHandler.class);
        final AgentExecutionHandler<?> optimizerHandler = mock(AgentExecutionHandler.class);
        when(this.context.getBeansOfType(AgentExecutionHandler.class))
                .thenReturn(Map.of(
                        "userAgentExecutionHandler", userHandler,
                        "ruleSuggestionAnalyzerAgentExecutionHandler", analyzerHandler,
                        "contextOptimizerAgentExecutionHandler", optimizerHandler
                ));

        //when
        this.agentTypeHandlerInjector.injectHandlers();

        //then
        assertThat(AgentType.USER.getHandler()).isEqualTo(userHandler);
        assertThat(AgentType.SYSTEM_RULE_ANALYZER.getHandler()).isEqualTo(analyzerHandler);
        assertThat(AgentType.SYSTEM_CONTEXT_OPTIMIZER.getHandler()).isEqualTo(optimizerHandler);
        verify(this.context).getBeansOfType(AgentExecutionHandler.class);
    }

    @Test
    void givenMissingHandlerBean_whenInjectHandlers_thenThrowIllegalStateException() {
        //given
        when(this.context.getBeansOfType(AgentExecutionHandler.class))
                .thenReturn(Map.of(
                        "userAgentExecutionHandler", mock(AgentExecutionHandler.class)
                ));

        //when
        //then
        assertThatThrownBy(() -> this.agentTypeHandlerInjector.injectHandlers())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No AgentExecutionHandler bean for type: ruleSuggestionAnalyzerAgentExecutionHandler");
        verify(this.context).getBeansOfType(AgentExecutionHandler.class);
    }
}
