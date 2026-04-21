package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.repository.AgentRepository;
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
    private AgentRepository agentRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentConversation = new GetAgentConversationImpl(
                this.agentRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenVisibleConversation_whenExecute_thenReturnConversationDetails() {
        //given
        final UUID agentId = UUID.fromString("41111111-1111-1111-1111-111111111111");
        final UUID conversationId = UUID.fromString("51111111-1111-1111-1111-111111111111");
        final Agent agent = mock(Agent.class);
        final Conversation conversation = mock(Conversation.class);
        final List<ConversationMessage> messages = List.of(mock(ConversationMessage.class));

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.of(conversation));
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(messages);

        //when
        final ConversationDetails actual = this.getAgentConversation.execute(agentId, conversationId);

        //then
        assertThat(actual).isEqualTo(ConversationDetails.builder()
                .conversation(conversation)
                .messages(messages)
                .build());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Test
    void givenMissingConversation_whenExecute_thenThrowAgentNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("61111111-1111-1111-1111-111111111111");
        final UUID conversationId = UUID.fromString("71111111-1111-1111-1111-111111111111");
        final Agent agent = mock(Agent.class);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getAgentConversation.execute(agentId, conversationId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(conversationId, 17L, agentId);
        verifyNoInteractions(this.conversationMessageRepository);
    }
}
