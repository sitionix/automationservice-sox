package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.ConversationChatHandler;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAgentImplTest {

    private ChatAgentImpl chatAgent;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.chatAgent = new ChatAgentImpl(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        ConversationType.DIRECT.setHandler(null);
        verifyNoMoreInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenNewConversationRequest_whenExecute_thenCreateConversationAndDelegateToTypeHandler() {
        //given
        final UUID agentId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84ca");
        final UUID conversationId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb");
        final Instant now = Instant.parse("2026-04-20T08:05:00Z");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .message("  Explain clean architecture.  ")
                .build();
        final Conversation createdConversation = this.getConversation(conversationId, now);
        final ConversationChatHandler handler = mock(ConversationChatHandler.class);
        final ChatAgentResponse expected = ChatAgentResponse.builder()
                .conversationId(conversationId)
                .build();
        ConversationType.DIRECT.setHandler(handler);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(createdConversation);
        when(handler.handle(any(Conversation.class), any(List.class), any(ChatAgentCommand.class), any(Long.class)))
                .thenReturn(expected);

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual).isEqualTo(expected);
        final ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(this.conversationRepository).save(conversationCaptor.capture());
        final Conversation savedConversation = conversationCaptor.getValue();
        assertThat(savedConversation.getId()).isNotNull();
        assertThat(savedConversation.getUserId()).isEqualTo(17L);
        assertThat(savedConversation.getTitle()).isEqualTo("Explain clean architecture.");
        assertThat(savedConversation.getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(savedConversation.getStatus()).isEqualTo(ConversationStatus.ACTIVE);

        final ArgumentCaptor<List<ConversationParticipant>> participantsCaptor = ArgumentCaptor.forClass(List.class);
        verify(this.conversationParticipantRepository).saveAll(participantsCaptor.capture());
        final List<ConversationParticipant> participants = participantsCaptor.getValue();
        assertThat(participants).hasSize(2);
        assertThat(participants.get(0).getParticipantType()).isEqualTo(ConversationParticipantType.USER);
        assertThat(participants.get(0).getParticipantId()).isEqualTo("17");
        assertThat(participants.get(1).getParticipantType()).isEqualTo(ConversationParticipantType.AGENT);
        assertThat(participants.get(1).getParticipantId()).isEqualTo(agentId.toString());

        final ArgumentCaptor<ChatAgentCommand> normalizedCommandCaptor = ArgumentCaptor.forClass(ChatAgentCommand.class);
        verify(handler).handle(
                eq(createdConversation),
                eq(participants),
                normalizedCommandCaptor.capture(),
                eq(17L)
        );
        final ChatAgentCommand normalizedCommand = normalizedCommandCaptor.getValue();
        assertThat(normalizedCommand.getConversationId()).isEqualTo(conversationId);
        assertThat(normalizedCommand.getMessage()).isEqualTo("Explain clean architecture.");

        verify(this.authenticatedUserProvider).getUserId();
        verifyNoMoreInteractions(handler);
    }

    @Test
    void givenExistingConversationRequest_whenExecute_thenLoadConversationAndDelegateToTypeHandler() {
        //given
        final UUID agentId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebf9");
        final UUID conversationId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebfa");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message("hello")
                .build();
        final Conversation conversation = this.getConversation(conversationId, Instant.parse("2026-04-20T08:05:00Z"));
        final List<ConversationParticipant> participants = List.of(
                this.getParticipant(conversationId, ConversationParticipantType.USER, "17"),
                this.getParticipant(conversationId, ConversationParticipantType.AGENT, agentId.toString())
        );
        final ConversationChatHandler handler = mock(ConversationChatHandler.class);
        final ChatAgentResponse expected = ChatAgentResponse.builder()
                .conversationId(conversationId)
                .build();
        ConversationType.DIRECT.setHandler(handler);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L)).thenReturn(Optional.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId)).thenReturn(participants);
        when(handler.handle(conversation, participants, ChatAgentCommand.builder().conversationId(conversationId).message("hello").build(), 17L))
                .thenReturn(expected);

        //when
        final ChatAgentResponse actual = this.chatAgent.execute(agentId, command);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(handler).handle(conversation, participants, ChatAgentCommand.builder().conversationId(conversationId).message("hello").build(), 17L);
        verifyNoMoreInteractions(handler);
    }

    @Test
    void givenMissingConversation_whenExecute_thenThrowNotFound() {
        //given
        final UUID agentId = UUID.fromString("a28ea11a-aa2c-4ec8-b790-9b6c9b7f5639");
        final UUID conversationId = UUID.fromString("b28ea11a-aa2c-4ec8-b790-9b6c9b7f5639");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message("hello")
                .build();

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.chatAgent.execute(agentId, command))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verifyNoInteractions(this.conversationParticipantRepository);
    }

    @Test
    void givenBlankMessage_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("704f2908-c499-4669-8fa5-c8cc37f4620f");
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .message("   ")
                .build();

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.chatAgent.execute(agentId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.conversationRepository, this.conversationParticipantRepository);
    }

    private Conversation getConversation(final UUID conversationId, final Instant createdAt) {
        return Conversation.builder()
                .id(conversationId)
                .userId(17L)
                .title("Explain clean architecture.")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .lastMessageAt(createdAt)
                .build();
    }

    private ConversationParticipant getParticipant(final UUID conversationId,
                                                   final ConversationParticipantType type,
                                                   final String participantId) {
        return ConversationParticipant.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .participantType(type)
                .participantId(participantId)
                .joinedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
