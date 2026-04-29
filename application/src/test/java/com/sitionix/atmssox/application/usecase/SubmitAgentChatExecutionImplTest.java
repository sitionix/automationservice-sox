package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentAccessDeniedException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitAgentChatExecutionImplTest {

    private SubmitAgentChatExecutionImpl submitAgentChatExecution;

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;
    @Mock
    private ChatExecutionRepository chatExecutionRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @Mock
    private ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @BeforeEach
    void setUp() {
        this.submitAgentChatExecution = new SubmitAgentChatExecutionImpl(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider,
                this.chatExecutionAsyncProcessor
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider,
                this.chatExecutionAsyncProcessor
        );
    }

    @Test
    void givenValidRequestWithoutConversationId_whenExecute_thenCreateQueuedExecutionAndScheduleAsyncProcessing() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final UUID executionId = UUID.fromString("66fbb67c-ef6f-4cde-b5bc-f4955de0181e");
        final ChatAgentCommand command = ChatAgentCommand.builder().message("  hello world  ").build();
        final Conversation createdConversation = this.getConversation(conversationId, 17L);
        final ChatExecution savedExecution = ChatExecution.builder()
                .executionId(executionId)
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(17L)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage("hello world")
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .build();

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(createdConversation);
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
        assertThat(toSave.getConversationId()).isEqualTo(conversationId);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).save(any(Conversation.class));
        verify(this.conversationParticipantRepository).saveAll(any());
        verify(this.chatExecutionAsyncProcessor).processAsync(executionId);
    }

    @Test
    void givenExistingIdempotencyForSameConversation_whenExecute_thenReturnReplayedExecution() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final ChatAgentCommand command = ChatAgentCommand.builder().conversationId(conversationId).message("hello").build();
        final ChatExecution existing = ChatExecution.builder()
                .executionId(UUID.fromString("66fbb67c-ef6f-4cde-b5bc-f4955de0181e"))
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(17L)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage("hello")
                .idempotencyKey("KEY")
                .idempotencyReplayed(false)
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .build();

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
        verifyNoInteractions(this.chatExecutionAsyncProcessor);
    }

    @Test
    void givenForeignConversation_whenExecute_thenThrowAccessDenied() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final UUID conversationId = UUID.fromString("27acb3bb-2f98-4db8-9fd2-f62d10256c29");
        final ChatAgentCommand command = ChatAgentCommand.builder().conversationId(conversationId).message("hello").build();

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
        verifyNoInteractions(this.chatExecutionRepository, this.chatExecutionAsyncProcessor, this.conversationParticipantRepository);
    }

    @Test
    void givenBlankMessage_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("f5dbbe40-6399-4aa8-8ee4-20ce8fd3724d");
        final ChatAgentCommand command = ChatAgentCommand.builder().message(" ").build();

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.submitAgentChatExecution.execute(agentId, command, null))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.conversationRepository, this.conversationParticipantRepository, this.chatExecutionRepository, this.chatExecutionAsyncProcessor);
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
}
