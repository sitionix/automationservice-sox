package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcreteCapabilityExecutionServiceTest {

    private ConcreteCapabilityExecutionService concreteCapabilityExecutionService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private CapabilityHandler<Object> capabilityHandler;

    @BeforeEach
    void setUp() throws Exception {
        this.concreteCapabilityExecutionService = new ConcreteCapabilityExecutionService(
                new ObjectMapper(),
                new CapabilityToolPayloadCodec(new ObjectMapper()),
                this.authenticatedUserProvider
        );
        CapabilityName.GET_WORKSPACE_SITES.setHandler(this.capabilityHandler);
    }

    @AfterEach
    void tearDown() {
        CapabilityName.GET_WORKSPACE_SITES.setHandler(null);
        verifyNoMoreInteractions(this.authenticatedUserProvider, this.capabilityHandler);
    }

    @Test
    void givenValidCapabilityCall_whenExecute_thenReturnSerializedPayload() throws Exception {
        //given
        final OpenAiNativeToolCall givenToolCall = new OpenAiNativeToolCall("c1", "GET_WORKSPACE_SITES", "{}");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.capabilityHandler.argType()).thenReturn(Object.class);
        final JsonNode payload = new ObjectMapper().readTree("{\"ok\":true}");
        when(this.capabilityHandler.execute(any(CapabilityExecutionCommand.class)))
                .thenReturn(new CapabilityExecutionResult(CapabilityName.GET_WORKSPACE_SITES, payload));

        //when
        final OpenAiNativeToolResult actual = this.concreteCapabilityExecutionService.execute(givenToolCall);

        //then
        assertThat(actual.callId()).isEqualTo("c1");
        assertThat(actual.outputJson()).contains("\"ok\":true");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.capabilityHandler).argType();
        verify(this.capabilityHandler).execute(any(CapabilityExecutionCommand.class));
    }

    @Test
    void givenInvalidCapabilityCall_whenExecute_thenReturnErrorPayload() {
        //given
        final OpenAiNativeToolCall givenToolCall = new OpenAiNativeToolCall("c1", "UNKNOWN", "{}");

        //when
        final OpenAiNativeToolResult actual = this.concreteCapabilityExecutionService.execute(givenToolCall);

        //then
        assertThat(actual.callId()).isEqualTo("c1");
        assertThat(actual.outputJson()).contains("error");
    }
}
