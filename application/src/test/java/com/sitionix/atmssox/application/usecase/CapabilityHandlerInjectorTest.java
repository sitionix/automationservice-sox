package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityHandlerInjectorTest {

    private CapabilityHandlerInjector capabilityHandlerInjector;

    @Mock
    private ApplicationContext context;

    @Mock
    private CapabilityHandler<Object> getSiteOverviewCapabilityHandler;

    @Mock
    private CapabilityHandler<Object> getWorkspaceSitesCapabilityHandler;

    @BeforeEach
    void setUp() {
        this.capabilityHandlerInjector = new CapabilityHandlerInjector(this.context);
    }

    @AfterEach
    void tearDown() {
        CapabilityName.GET_SITE_OVERVIEW.setHandler(null);
        CapabilityName.GET_WORKSPACE_SITES.setHandler(null);
        verifyNoMoreInteractions(this.context);
        verifyNoMoreInteractions(this.getSiteOverviewCapabilityHandler);
        verifyNoMoreInteractions(this.getWorkspaceSitesCapabilityHandler);
    }

    @Test
    void givenAllHandlersPresent_whenInjectHandlers_thenAssignHandlersToCapabilityNames() {
        //given
        when(this.context.getBeansOfType(CapabilityHandler.class)).thenReturn(Map.of(
                CapabilityName.GET_SITE_OVERVIEW.getBindingKey(), this.getSiteOverviewCapabilityHandler,
                CapabilityName.GET_WORKSPACE_SITES.getBindingKey(), this.getWorkspaceSitesCapabilityHandler
        ));

        //when
        this.capabilityHandlerInjector.injectHandlers();

        //then
        assertThat(CapabilityName.GET_SITE_OVERVIEW.getHandler()).isEqualTo(this.getSiteOverviewCapabilityHandler);
        assertThat(CapabilityName.GET_WORKSPACE_SITES.getHandler()).isEqualTo(this.getWorkspaceSitesCapabilityHandler);
        verify(this.context).getBeansOfType(CapabilityHandler.class);
    }

    @Test
    void givenMissingHandler_whenInjectHandlers_thenThrowIllegalStateException() {
        //given
        when(this.context.getBeansOfType(CapabilityHandler.class)).thenReturn(Map.of(
                CapabilityName.GET_SITE_OVERVIEW.getBindingKey(), this.getSiteOverviewCapabilityHandler
        ));

        //when
        //then
        assertThatThrownBy(() -> this.capabilityHandlerInjector.injectHandlers())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No CapabilityHandler bean for type: " + CapabilityName.GET_WORKSPACE_SITES.getBindingKey());
        verify(this.context).getBeansOfType(CapabilityHandler.class);
    }
}
