package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAgentConversationImplTest {

    private DeleteAgentConversationImpl deleteAgentConversation;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.deleteAgentConversation = new DeleteAgentConversationImpl(this.conversationRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.conversationRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenActiveConversation_whenExecute_thenSaveWithDeletedStatus() {
        //given
        final UUID givenConversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long givenUserId = 7L;
        final Conversation current = this.getConversation(ConversationStatus.ACTIVE);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(givenUserId);
        when(this.conversationRepository.findByIdAndUserId(givenConversationId, givenUserId)).thenReturn(Optional.of(current));
        when(this.conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        this.deleteAgentConversation.execute(givenConversationId);

        //then
        final ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findByIdAndUserId(givenConversationId, givenUserId);
        verify(this.conversationRepository).save(conversationCaptor.capture());

        final Conversation saved = conversationCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(current.getId());
        assertThat(saved.getUserId()).isEqualTo(current.getUserId());
        assertThat(saved.getTitle()).isEqualTo(current.getTitle());
        assertThat(saved.getType()).isEqualTo(current.getType());
        assertThat(saved.getCreatedAt()).isEqualTo(current.getCreatedAt());
        assertThat(saved.getLastMessageAt()).isEqualTo(current.getLastMessageAt());
        assertThat(saved.getStatus()).isEqualTo(ConversationStatus.DELETED);
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(current.getUpdatedAt());
    }

    @Test
    void givenAlreadyDeletedConversation_whenExecute_thenDoNotSave() {
        //given
        final UUID givenConversationId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final Long givenUserId = 7L;
        final Conversation current = this.getConversation(ConversationStatus.DELETED);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(givenUserId);
        when(this.conversationRepository.findByIdAndUserId(givenConversationId, givenUserId)).thenReturn(Optional.of(current));

        //when
        this.deleteAgentConversation.execute(givenConversationId);

        //then
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findByIdAndUserId(givenConversationId, givenUserId);
    }

    @Test
    void givenUnknownConversation_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID givenConversationId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        final Long givenUserId = 7L;

        when(this.authenticatedUserProvider.getUserId()).thenReturn(givenUserId);
        when(this.conversationRepository.findByIdAndUserId(givenConversationId, givenUserId)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.deleteAgentConversation.execute(givenConversationId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findByIdAndUserId(givenConversationId, givenUserId);
    }

    private Conversation getConversation(final ConversationStatus status) {
        return Conversation.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .userId(7L)
                .title("Conversation title")
                .type(ConversationType.DIRECT)
                .status(status)
                .createdAt(Instant.parse("2026-05-01T09:00:00Z"))
                .updatedAt(Instant.parse("2026-05-01T09:00:00Z"))
                .lastMessageAt(Instant.parse("2026-05-01T09:05:00Z"))
                .build();
    }
}
