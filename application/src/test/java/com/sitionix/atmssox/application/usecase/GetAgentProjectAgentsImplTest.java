package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentProjectAgentsImplTest {

    private GetAgentProjectAgentsImpl getAgentProjectAgents;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentProjectAgents = new GetAgentProjectAgentsImpl(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.agentProjectMemberRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleProject_whenExecute_thenReturnProjectAgents() {
        //given
        final UUID projectId = UUID.fromString("a6ce6f31-11ed-469a-bd53-d1119ce67fb5");
        final Long userId = 17L;
        final AgentProject project = mock(AgentProject.class);
        final ProjectAgent projectAgent = mock(ProjectAgent.class);
        final List<ProjectAgent> expected = List.of(projectAgent);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId)).thenReturn(expected);

        //when
        final List<ProjectAgent> actual = this.getAgentProjectAgents.execute(projectId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, userId);
    }

    @Test
    void givenUnknownProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("36b56387-8a5f-45b4-9366-f9f8d57cb6d4");
        final Long userId = 17L;
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.getAgentProjectAgents.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
    }
}
