package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentProjectImplTest {

    private GetAgentProjectImpl getAgentProject;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentProject = new GetAgentProjectImpl(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleProjectForCurrentUser_whenExecute_thenReturnProject() {
        //given
        final UUID projectId = UUID.fromString("778e3e48-e8b2-4ee4-83fc-0df741748b34");
        final AgentProject expected = mock(AgentProject.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(expected));

        //when
        final AgentProject actual = this.getAgentProject.execute(projectId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenUnknownOrHiddenProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("f688091f-b7a4-4ecc-b81d-551d8519f5c9");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.getAgentProject.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }
}
