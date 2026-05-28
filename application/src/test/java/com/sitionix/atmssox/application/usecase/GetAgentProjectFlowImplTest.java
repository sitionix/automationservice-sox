package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.repository.AgentProjectFlowRepository;
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
class GetAgentProjectFlowImplTest {

    private GetAgentProjectFlowImpl getAgentProjectFlow;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private AgentProjectFlowRepository agentProjectFlowRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentProjectFlow = new GetAgentProjectFlowImpl(this.agentProjectRepository, this.agentProjectFlowRepository,
                this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.agentProjectFlowRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleProjectAndPersistedFlow_whenExecute_thenReturnPersistedFlow() {
        //given
        final UUID projectId = UUID.fromString("1270ffb7-fa4c-49a9-a6d6-f65a960d3f67");
        final Long userId = 17L;
        final AgentProject project = mock(AgentProject.class);
        final AgentProjectFlow persistedFlow = mock(AgentProjectFlow.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(this.agentProjectFlowRepository.findByProjectId(projectId)).thenReturn(Optional.of(persistedFlow));

        //when
        final AgentProjectFlow actual = this.getAgentProjectFlow.execute(projectId);

        //then
        assertThat(actual).isEqualTo(persistedFlow);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
        verify(this.agentProjectFlowRepository).findByProjectId(projectId);
    }

    @Test
    void givenVisibleProjectAndNoPersistedFlow_whenExecute_thenReturnEmptyFlowWithoutWrite() {
        //given
        final UUID projectId = UUID.fromString("8b854005-f6f7-40bb-ab9f-3d59f92f97e8");
        final Long userId = 17L;
        final AgentProject project = mock(AgentProject.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(this.agentProjectFlowRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        //when
        final AgentProjectFlow actual = this.getAgentProjectFlow.execute(projectId);

        //then
        assertThat(actual).isEqualTo(AgentProjectFlow.builder().projectId(projectId).flowId(null).nodes(List.of()).edges(List.of()).build());
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
        verify(this.agentProjectFlowRepository).findByProjectId(projectId);
    }

    @Test
    void givenUnknownProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("f7b45312-a0c4-4d89-a4ce-2f9a54865d14");
        final Long userId = 17L;
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.getAgentProjectFlow.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
    }
}
