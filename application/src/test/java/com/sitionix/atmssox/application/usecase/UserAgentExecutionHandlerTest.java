package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAgentExecutionHandlerTest {

    private UserAgentExecutionHandler userAgentExecutionHandler;

    @Mock
    private CapabilityToolLoopService capabilityToolLoopService;

    @Mock
    private AutomationCapabilitiesProperties automationCapabilitiesProperties;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @BeforeEach
    void setUp() {
        this.userAgentExecutionHandler = new UserAgentExecutionHandler(
                this.capabilityToolLoopService,
                this.automationCapabilitiesProperties,
                this.openAiChatClient
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.capabilityToolLoopService, this.automationCapabilitiesProperties, this.openAiChatClient);
    }

    @Test
    void givenValidAgentAndPrompt_whenExecute_thenExecuteCapabilityToolLoopServiceWithNormalizedInput() {
        //given
        final Agent givenAgent = this.getAgent("  Keep answers concise.  ");
        final UserAgentExecutionContext givenContext = new UserAgentExecutionContext(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "  Keep answers concise.  ",
                "  Explain SOLID.  "
        );
        when(this.automationCapabilitiesProperties.isEnabled()).thenReturn(true);
        when(this.capabilityToolLoopService.execute(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "Keep answers concise.",
                "Explain SOLID."
        )).thenReturn("answer");

        //when
        final String actual = this.userAgentExecutionHandler.execute(givenAgent, givenContext);

        //then
        assertThat(actual).isEqualTo("answer");
        verify(this.automationCapabilitiesProperties).isEnabled();
        verify(this.capabilityToolLoopService).execute(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "Keep answers concise.",
                "Explain SOLID."
        );
    }

    @Test
    void givenCapabilitiesDisabled_whenExecute_thenFallbackToPlainOpenAiExecution() {
        //given
        final Agent givenAgent = this.getAgent("  Keep answers concise.  ");
        final UserAgentExecutionContext givenContext = new UserAgentExecutionContext(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "  Keep answers concise.  ",
                "  Explain SOLID.  "
        );
        when(this.automationCapabilitiesProperties.isEnabled()).thenReturn(false);
        when(this.openAiChatClient.execute(new OpenAiChatRequest("Keep answers concise.", "Explain SOLID."))).thenReturn("answer");
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);

        //when
        final String actual = this.userAgentExecutionHandler.execute(givenAgent, givenContext);

        //then
        assertThat(actual).isEqualTo("answer");
        verify(this.automationCapabilitiesProperties).isEnabled();
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        final OpenAiChatRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.instruction()).isEqualTo("Keep answers concise.");
        assertThat(actualRequest.instruction())
                .doesNotContain("You can use backend platform capabilities through tools.");
        assertThat(actualRequest.input()).isEqualTo("Explain SOLID.");
    }

    @Test
    void givenBlankPrompt_whenExecute_thenThrowAgentValidationException() {
        //given
        final Agent givenAgent = this.getAgent("Instruction");
        final UserAgentExecutionContext givenContext = new UserAgentExecutionContext(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "Instruction",
                "   "
        );

        //when
        //then
        assertThatThrownBy(() -> this.userAgentExecutionHandler.execute(givenAgent, givenContext))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("User prompt is empty");
    }

    @Test
    void givenCapabilityLoopFails_whenExecute_thenFallbackToPlainOpenAiExecution() {
        //given
        final Agent givenAgent = this.getAgent("  Keep answers concise.  ");
        final UserAgentExecutionContext givenContext = new UserAgentExecutionContext(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "  Keep answers concise.  ",
                "  Explain SOLID.  "
        );
        when(this.automationCapabilitiesProperties.isEnabled()).thenReturn(true);
        when(this.capabilityToolLoopService.execute(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "Keep answers concise.",
                "Explain SOLID."
        ))
                .thenThrow(new RuntimeException("tool loop fail"));
        when(this.openAiChatClient.execute(new OpenAiChatRequest("Keep answers concise.", "Explain SOLID."))).thenReturn("fallback");

        //when
        final String actual = this.userAgentExecutionHandler.execute(givenAgent, givenContext);

        //then
        assertThat(actual).isEqualTo("fallback");
        verify(this.automationCapabilitiesProperties).isEnabled();
        verify(this.capabilityToolLoopService).execute(
                UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"),
                UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb"),
                "Keep answers concise.",
                "Explain SOLID."
        );
        verify(this.openAiChatClient).execute(new OpenAiChatRequest("Keep answers concise.", "Explain SOLID."));
    }

    private Agent getAgent(final String instruction) {
        return Agent.builder()
                .id(UUID.fromString("49d7c30a-9ea5-4ff5-a66e-ae5373fc214c"))
                .userId(17L)
                .name("Name")
                .description("Description")
                .instruction(instruction)
                .type(AgentType.USER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
