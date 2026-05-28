package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPaletteSource;
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
class GetAgentProjectFlowPaletteImplTest {

    private GetAgentProjectFlowPaletteImpl getAgentProjectFlowPalette;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentProjectFlowPalette = new GetAgentProjectFlowPaletteImpl(this.agentProjectRepository, this.agentProjectMemberRepository,
                this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.agentProjectMemberRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleProjectAndVisibleAgents_whenExecute_thenReturnUserSourceAndAgentSources() {
        //given
        final UUID projectId = UUID.fromString("eb0d4212-d8a0-4ea6-a9c0-c91cbc9a0472");
        final UUID firstAgentId = UUID.fromString("770b41cc-66e4-4ab1-b994-e393f29148ff");
        final UUID secondAgentId = UUID.fromString("1ab5eb44-e478-42d5-a7f9-67ca6d84b5de");
        final Long userId = 17L;
        final AgentProject project = mock(AgentProject.class);
        final ProjectAgent firstProjectAgent = ProjectAgent.builder().id(firstAgentId).name("A").build();
        final ProjectAgent secondProjectAgent = ProjectAgent.builder().id(secondAgentId).name("B").build();
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId)).thenReturn(List.of(firstProjectAgent, secondProjectAgent));

        //when
        final AgentProjectFlowPalette actual = this.getAgentProjectFlowPalette.execute(projectId);

        //then
        assertThat(actual.getSources()).isEqualTo(List.of(
                AgentProjectFlowPaletteSource.builder().sourceType("USER").sourceId(null).sourceName("USER").build(),
                AgentProjectFlowPaletteSource.builder().sourceType("AGENT").sourceId(firstAgentId).sourceName("A").build(),
                AgentProjectFlowPaletteSource.builder().sourceType("AGENT").sourceId(secondAgentId).sourceName("B").build()
        ));
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, userId);
    }

    @Test
    void givenUnknownProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("f8f3fab9-f8ef-4377-a1f9-af7bb8c73342");
        final Long userId = 17L;
        when(this.authenticatedUserProvider.getUserId()).thenReturn(userId);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.getAgentProjectFlowPalette.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
    }
}
