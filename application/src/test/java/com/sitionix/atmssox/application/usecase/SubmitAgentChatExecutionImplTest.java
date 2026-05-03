package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentAccessDeniedException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitAgentChatExecutionImplTest {

    private SubmitAgentChatExecutionImpl submitAgentChatExecution;

    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationParticipantRepository conversationParticipantRepository;
    @Mock private ConversationMessageRepository conversationMessageRepository;
    @Mock private ChatExecutionRepository chatExecutionRepository;
    @Mock private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        this.submitAgentChatExecution = new SubmitAgentChatExecutionImpl(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider,
                this.applicationEventPublisher
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider,
                this.applicationEventPublisher
        );
    }

    @Test
    void givenValidRequestWithoutConversationId_whenExecute_thenCreateQueuedExecutionAndScheduleAsyncProcessing() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final UUID executionId = UUID.fromString("66fbb67c-ef6f-4cde-b5bc-f4955de0181e");
        final ChatAgentCommand command = this.getChatAgentCommand(null, "  hello world  ");
        final Conversation createdConversation = this.getConversation(conversationId, 17L);
        final ChatExecution savedExecution = this.getQueuedChatExecution(
                executionId,
                agentId,
                conversationId,
                17L,
                "hello world",
                "2026-04-29T00:00:00Z",
                "KEY"
        );

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(createdConversation);
        when(this.conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(this.getConversationMessage(conversationId, 17L, "hello world"));
        when(this.chatExecutionRepository.save(any(ChatExecution.class))).thenReturn(savedExecution);

        //when
        final ChatExecution actual = this.submitAgentChatExecution.execute(agentId, command, null);

        //then
        assertThat(actual).isEqualTo(savedExecution);
        final ArgumentCaptor<ChatExecution> executionCaptor = ArgumentCaptor.forClass(ChatExecution.class);
        verify(this.chatExecutionRepository).save(executionCaptor.capture());
        final ChatExecution toSave = executionCaptor.getValue();
        assertThat(toSave.getStatus()).isEqualTo(ChatExecutionStatus.QUEUED);
        assertThat(toSave.getRequestMessage()).isEqualTo("hello world");
        assertThat(toSave.getInputMessageId()).isNotNull();
        assertThat(toSave.getConversationId()).isEqualTo(conversationId);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository, times(2)).save(any(Conversation.class));
        verify(this.conversationMessageRepository).save(any(ConversationMessage.class));
        verify(this.conversationParticipantRepository).saveAll(any());
        verify(this.applicationEventPublisher).publishEvent(any(ChatExecutionSubmittedEvent.class));
    }

    @Test
    void givenExistingIdempotencyForSameConversation_whenExecute_thenReturnReplayedExecution() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final ChatAgentCommand command = this.getChatAgentCommand(conversationId, "hello");
        final ChatExecution existing = this.getQueuedChatExecution(
                UUID.fromString("66fbb67c-ef6f-4cde-b5bc-f4955de0181e"),
                agentId,
                conversationId,
                17L,
                "hello",
                "2026-04-29T00:00:00Z",
                "KEY"
        );

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.of(this.getConversation(conversationId, 17L)));
        when(this.chatExecutionRepository.findByUserIdAndAgentIdAndIdempotencyKey(17L, agentId, "KEY"))
                .thenReturn(Optional.of(existing));

        //when
        final ChatExecution actual = this.submitAgentChatExecution.execute(agentId, command, " KEY ");

        //then
        assertThat(actual.isIdempotencyReplayed()).isTrue();
        assertThat(actual.getExecutionId()).isEqualTo(existing.getExecutionId());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verify(this.chatExecutionRepository).findByUserIdAndAgentIdAndIdempotencyKey(17L, agentId, "KEY");
        verifyNoInteractions(this.applicationEventPublisher, this.conversationMessageRepository);
    }

    @Test
    void givenForeignConversation_whenExecute_thenThrowAccessDenied() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final ChatAgentCommand command = this.getChatAgentCommand(conversationId, "hello");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.empty());
        when(this.conversationRepository.findActiveByIdAndAgentId(conversationId, agentId))
                .thenReturn(Optional.of(this.getConversation(conversationId, 99L)));

        //when
        //then
        assertThatThrownBy(() -> this.submitAgentChatExecution.execute(agentId, command, null))
                .isInstanceOf(AgentAccessDeniedException.class)
                .hasMessage("Conversation is not accessible");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verify(this.conversationRepository).findActiveByIdAndAgentId(conversationId, agentId);
        verifyNoInteractions(this.chatExecutionRepository, this.applicationEventPublisher, this.conversationParticipantRepository, this.conversationMessageRepository);
    }

    @Test
    void givenExistingIdempotencyForAnotherConversation_whenExecute_thenThrowValidationConflict() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final UUID anotherConversationId = UUID.fromString("b2656439-c7fe-40a9-ab08-59bddce0f915");
        final ChatAgentCommand command = this.getChatAgentCommand(conversationId, "hello");
        final ChatExecution existing = this.getQueuedChatExecution(
                UUID.fromString("66fbb67c-ef6f-4cde-b5bc-f4955de0181e"),
                agentId,
                anotherConversationId,
                17L,
                "hello",
                "2026-04-29T00:00:00Z",
                "KEY"
        );

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.of(this.getConversation(conversationId, 17L)));
        when(this.chatExecutionRepository.findByUserIdAndAgentIdAndIdempotencyKey(17L, agentId, "KEY"))
                .thenReturn(Optional.of(existing));

        //when
        //then
        assertThatThrownBy(() -> this.submitAgentChatExecution.execute(agentId, command, " KEY "))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Idempotency key conflict");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verify(this.chatExecutionRepository).findByUserIdAndAgentIdAndIdempotencyKey(17L, agentId, "KEY");
        verifyNoInteractions(this.applicationEventPublisher, this.conversationParticipantRepository, this.conversationMessageRepository);
    }

    @Test
    void givenBlankMessage_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final ChatAgentCommand command = this.getChatAgentCommand(null, " ");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.submitAgentChatExecution.execute(agentId, command, null))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.conversationRepository, this.conversationParticipantRepository, this.chatExecutionRepository, this.applicationEventPublisher, this.conversationMessageRepository);
    }

    private Conversation getConversation(final UUID id, final Long userId) {
        return Conversation.builder()
                .id(id)
                .userId(userId)
                .title("title")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .updatedAt(Instant.parse("2026-04-29T00:00:00Z"))
                .lastMessageAt(Instant.parse("2026-04-29T00:00:00Z"))
                .build();
    }

    private ChatAgentCommand getChatAgentCommand(final UUID conversationId, final String message) {
        return ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message(message)
                .build();
    }

    private ChatExecution getQueuedChatExecution(
            final UUID executionId,
            final UUID agentId,
            final UUID conversationId,
            final Long userId,
            final String requestMessage,
            final String createdAt,
            final String idempotencyKey
    ) {
        return ChatExecution.builder()
                .executionId(executionId)
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(userId)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage(requestMessage)
                .idempotencyKey(idempotencyKey)
                .idempotencyReplayed(false)
                .inputMessageId(UUID.fromString("11f8d277-bbe4-4e6d-b334-ed2276e833dd"))
                .createdAt(Instant.parse(createdAt))
                .build();
    }

    private ConversationMessage getConversationMessage(final UUID conversationId, final Long userId, final String message) {
        return ConversationMessage.builder()
                .id(UUID.fromString("11f8d277-bbe4-4e6d-b334-ed2276e833dd"))
                .conversationId(conversationId)
                .authorId(String.valueOf(userId))
                .content(message)
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .build();
    }
}
