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
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationAuthorType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private ConversationContextBuilder conversationContextBuilder;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @BeforeEach
    void setUp() {
        this.chatAgent = new ChatAgentImpl(
                this.agentRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider,
                this.conversationContextBuilder,
                this.openAiChatClient
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider,
                this.conversationContextBuilder,
                this.openAiChatClient
        );
    }

    @Test
    void givenActiveAgentAndValidMessage_whenExecute_thenReturnReplyWithTrimmedPayload() {
        //given
        final UUID agentId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84ca");
        final UUID conversationId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb");
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, "  Keep answers concise.  ");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .message("  Explain clean architecture.  ")
                .build();
        final Conversation conversation = this.getConversation(conversationId);
        final ConversationMessage userMessage = this.getMessage(
                conversationId,
                ConversationAuthorType.USER,
                "17",
                "Explain clean architecture."
        );
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(this.conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(userMessage)
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(userMessage));
        when(this.conversationContextBuilder.build(List.of(userMessage))).thenReturn("context-prompt");
        when(this.openAiChatClient.execute("Keep answers concise.", "context-prompt"))
                .thenReturn("It separates business rules from external frameworks.");

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual.getConversationId()).isEqualTo(conversationId);
        assertThat(actual.getReply().getAuthorType()).isEqualTo(ConversationAuthorType.AGENT);
        assertThat(actual.getReply().getAuthorId()).isEqualTo(agentId.toString());
        assertThat(actual.getReply().getContent()).isEqualTo("It separates business rules from external frameworks.");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.conversationRepository, times(2)).save(any(Conversation.class));
        verify(this.conversationParticipantRepository).saveAll(any(List.class));
        verify(this.conversationMessageRepository, times(2)).save(any(ConversationMessage.class));
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        verify(this.conversationContextBuilder).build(List.of(userMessage));
        verify(this.openAiChatClient).execute("Keep answers concise.", "context-prompt");
    }

    @Test
    void givenActiveAgentAndNullInstruction_whenExecute_thenCallProviderWithEmptyInstruction() {
        //given
        final UUID agentId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebf9");
        final UUID conversationId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebfa");
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, null);
        final Conversation conversation = this.getConversation(conversationId);
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message("hello")
                .build();
        final ConversationMessage userMessage = this.getMessage(conversationId, ConversationAuthorType.USER, "17", "hello");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.of(conversation));
        when(this.conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(userMessage)
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(userMessage));
        when(this.conversationContextBuilder.build(List.of(userMessage))).thenReturn("context-prompt");
        when(this.openAiChatClient.execute("", "context-prompt")).thenReturn("hi");
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual.getConversationId()).isEqualTo(conversationId);
        assertThat(actual.getReply().getContent()).isEqualTo("hi");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verify(this.conversationMessageRepository, times(2)).save(any(ConversationMessage.class));
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        verify(this.conversationContextBuilder).build(List.of(userMessage));
        verify(this.openAiChatClient).execute("", "context-prompt");
        verify(this.conversationRepository).save(any(Conversation.class));
        verify(this.conversationRepository, never()).findAllActiveByUserIdAndAgentId(any(), any());
        verifyNoInteractions(this.conversationParticipantRepository);
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
        verifyNoInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.openAiChatClient
        );
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
        verifyNoInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.openAiChatClient
        );
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
        verifyNoInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.openAiChatClient
        );
    }

    private Conversation getConversation(final UUID conversationId) {
        return Conversation.builder()
                .id(conversationId)
                .userId(17L)
                .title("Explain clean architecture.")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .lastMessageAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private ConversationMessage getMessage(final UUID conversationId,
                                           final ConversationAuthorType authorType,
                                           final String authorId,
                                           final String content) {
        return ConversationMessage.builder()
                .id(UUID.fromString("8a74f23d-ab2e-4ac8-b656-c76deec45f4f"))
                .conversationId(conversationId)
                .authorType(authorType)
                .authorId(authorId)
                .content(content)
                .createdAt(Instant.parse("2026-04-20T08:06:00Z"))
                .build();
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
