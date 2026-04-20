package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAgentImplTest {

    private GetAgentImpl getAgent;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgent = new GetAgentImpl(this.agentRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenAgentIdAndAuthenticatedUser_whenExecute_thenReturnAgent() {
        //given
        final UUID given = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent expected = mock(Agent.class);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(given, 17L)).thenReturn(Optional.of(expected));

        //when
        final Agent actual = this.getAgent.execute(given);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(given, 17L);
    }

    @Test
    void givenMissingAgentForAuthenticatedUser_whenExecute_thenThrowAgentNotFoundException() {
        //given
        final UUID given = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(given, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getAgent.execute(given))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(given, 17L);
    }

    @Test
    void givenAuthenticatedUserProviderThrows_whenExecute_thenThrowAuthenticationRequiredException() {
        //given
        final UUID given = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(this.authenticatedUserProvider.getUserId()).thenThrow(new AuthenticationRequiredException("Authentication required"));

        //when
        //then
        assertThatThrownBy(() -> this.getAgent.execute(given))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");

        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.agentRepository);
    }
}
