package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
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
class ConversationTypeHandlerInjectorTest {

    private ConversationTypeHandlerInjector conversationTypeHandlerInjector;

    @Mock
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        this.conversationTypeHandlerInjector = new ConversationTypeHandlerInjector(this.context);
    }

    @AfterEach
    void tearDown() {
        ConversationType.DIRECT.setHandler(null);
        verifyNoMoreInteractions(this.context);
    }

    @Test
    void givenHandlerBeanMap_whenInjectHandlers_thenAssignHandlerToConversationType() {
        //given
        final ConversationChatHandler directConversationChatHandler = mock(ConversationChatHandler.class);
        when(this.context.getBeansOfType(ConversationChatHandler.class))
                .thenReturn(Map.of("directConversationChatHandler", directConversationChatHandler));

        //when
        this.conversationTypeHandlerInjector.injectHandlers();

        //then
        assertThat(ConversationType.DIRECT.getHandler()).isEqualTo(directConversationChatHandler);
        verify(this.context).getBeansOfType(ConversationChatHandler.class);
    }

    @Test
    void givenMissingHandlerBean_whenInjectHandlers_thenThrowIllegalStateException() {
        //given
        when(this.context.getBeansOfType(ConversationChatHandler.class))
                .thenReturn(Map.of());

        //when
        //then
        assertThatThrownBy(() -> this.conversationTypeHandlerInjector.injectHandlers())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No ConversationChatHandler bean for type: directConversationChatHandler");
        verify(this.context).getBeansOfType(ConversationChatHandler.class);
    }
}
