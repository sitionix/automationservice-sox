package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAgentsImplTest {

    private GetAgentsImpl getAgents;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ForgeUserClient forgeUserClient;

    @BeforeEach
    void setUp() {
        this.getAgents = new GetAgentsImpl(this.agentRepository, this.forgeUserClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.forgeUserClient);
    }

    @Test
    void givenAuthenticatedUser_whenExecute_thenReturnUserAgents() {
        //given
        final Agent firstAgent = mock(Agent.class);
        final Agent secondAgent = mock(Agent.class);
        final List<Agent> expected = List.of(firstAgent, secondAgent);

        when(this.forgeUserClient.getUserId()).thenReturn(17L);
        when(this.agentRepository.findAllByUserId(17L)).thenReturn(expected);

        //when
        final List<Agent> actual = this.getAgents.execute();

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findAllByUserId(17L);
    }

    @Test
    void givenForgeUserClientThrows_whenExecute_thenThrowAuthenticationRequiredException() {
        //given
        when(this.forgeUserClient.getUserId()).thenThrow(new RuntimeException("No auth context"));

        //when
        //then
        assertThatThrownBy(() -> this.getAgents.execute())
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");

        verify(this.forgeUserClient).getUserId();
        verifyNoInteractions(this.agentRepository);
    }
}
