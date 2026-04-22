package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.repository.AgentRepository;
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
class GetAgentConversationsImplTest {

    private GetAgentConversationsImpl getAgentConversations;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentConversations = new GetAgentConversationsImpl(
                this.agentRepository,
                this.conversationRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.conversationRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleAgent_whenExecute_thenReturnConversations() {
        //given
        final UUID agentId = UUID.fromString("21111111-1111-1111-1111-111111111111");
        final Agent agent = mock(Agent.class);
        final List<Conversation> expected = List.of(mock(Conversation.class));

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.conversationRepository.findAllActiveByUserIdAndAgentId(17L, agentId)).thenReturn(expected);

        //when
        final List<Conversation> actual = this.getAgentConversations.execute(agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.conversationRepository).findAllActiveByUserIdAndAgentId(17L, agentId);
    }

    @Test
    void givenMissingAgent_whenExecute_thenThrowAgentNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("31111111-1111-1111-1111-111111111111");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getAgentConversations.execute(agentId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verifyNoInteractions(this.conversationRepository);
    }
}
