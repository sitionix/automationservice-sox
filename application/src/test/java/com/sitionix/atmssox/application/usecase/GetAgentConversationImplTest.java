package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentConversationImplTest {

    private GetAgentConversationImpl getAgentConversation;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentConversation = new GetAgentConversationImpl(
                this.conversationRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.conversationRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenVisibleConversation_whenExecute_thenReturnConversationDetails() {
        //given
        final UUID conversationId = UUID.fromString("51111111-1111-1111-1111-111111111111");
        final Conversation conversation = mock(Conversation.class);
        final List<ConversationMessage> messages = List.of(mock(ConversationMessage.class));

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L))
                .thenReturn(Optional.of(conversation));
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(messages);

        //when
        final ConversationDetails actual = this.getAgentConversation.execute(conversationId);

        //then
        assertThat(actual).isEqualTo(ConversationDetails.builder()
                .conversation(conversation)
                .messages(messages)
                .build());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Test
    void givenMissingConversation_whenExecute_thenThrowAgentNotFoundException() {
        //given
        final UUID conversationId = UUID.fromString("71111111-1111-1111-1111-111111111111");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getAgentConversation.execute(conversationId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verifyNoInteractions(this.conversationMessageRepository);
    }
}
