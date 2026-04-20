package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAgentImplTest {

    private ChatAgentImpl chatAgent;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @BeforeEach
    void setUp() {
        this.chatAgent = new ChatAgentImpl(this.agentRepository, this.authenticatedUserProvider, this.openAiChatClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.authenticatedUserProvider, this.openAiChatClient);
    }

    @Test
    void givenActiveAgentAndValidMessage_whenExecute_thenReturnReplyWithTrimmedPayload() {
        //given
        final UUID agentId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84ca");
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, "  Keep answers concise.  ");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .message("  Explain clean architecture.  ")
                .build();
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.openAiChatClient.execute("Keep answers concise.", "Explain clean architecture."))
                .thenReturn("It separates business rules from external frameworks.");

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual).isEqualTo(ChatAgentResponse.builder()
                .reply("It separates business rules from external frameworks.")
                .build());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.openAiChatClient).execute("Keep answers concise.", "Explain clean architecture.");
    }

    @Test
    void givenActiveAgentAndNullInstruction_whenExecute_thenCallProviderWithEmptyInstruction() {
        //given
        final UUID agentId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebf9");
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, null);
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .message("hello")
                .build();
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.openAiChatClient.execute("", "hello")).thenReturn("hi");

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual).isEqualTo(ChatAgentResponse.builder().reply("hi").build());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.openAiChatClient).execute("", "hello");
    }

    @Test
    void givenAgentNotFoundForUserScope_whenExecute_thenThrowNotFound() {
        //given
        final UUID agentId = UUID.fromString("a28ea11a-aa2c-4ec8-b790-9b6c9b7f5639");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.chatAgent.execute(agentId, ChatAgentCommand.builder().message("hello").build()))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verifyNoInteractions(this.openAiChatClient);
    }

    @Test
    void givenNonActiveAgent_whenExecute_thenThrowChatNotAllowed() {
        //given
        final UUID agentId = UUID.fromString("a88c40e3-28e5-4a9a-a292-83cc10c4f794");
        final Agent agent = this.getAgent(AgentStatus.DRAFT, "Instruction");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));

        //when
        //then
        assertThatThrownBy(() -> this.chatAgent.execute(agentId, ChatAgentCommand.builder().message("hello").build()))
                .isInstanceOf(AgentChatNotAllowedException.class)
                .hasMessage("Only ACTIVE agent can execute chat");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verifyNoInteractions(this.openAiChatClient);
    }

    @Test
    void givenBlankMessage_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("704f2908-c499-4669-8fa5-c8cc37f4620f");
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, "Instruction");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));

        //when
        //then
        assertThatThrownBy(() -> this.chatAgent.execute(agentId, ChatAgentCommand.builder().message("   ").build()))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verifyNoInteractions(this.openAiChatClient);
    }

    private Agent getAgent(final AgentStatus status, final String instruction) {
        return Agent.builder()
                .id(UUID.fromString("8a74f23d-ab2e-4ac8-b656-c76deec45f4e"))
                .userId(17L)
                .name("Architecture Agent")
                .description("Description")
                .instruction(instruction)
                .status(status)
                .createdAt(Instant.parse("2026-04-20T08:00:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:01:00Z"))
                .build();
    }
}
