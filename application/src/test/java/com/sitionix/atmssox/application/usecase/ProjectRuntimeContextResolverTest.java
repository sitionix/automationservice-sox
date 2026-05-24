package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectRuntimeContextResolverTest {

    private ProjectRuntimeContextResolver projectRuntimeContextResolver;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @BeforeEach
    void setUp() {
        this.projectRuntimeContextResolver = new ProjectRuntimeContextResolver(this.agentProjectRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository);
    }

    @Test
    void givenNullProjectId_whenResolve_thenReturnEmpty() {
        //given
        final Long userId = 17L;

        //when
        final Optional<ProjectRuntimeContext> actual = this.projectRuntimeContextResolver.resolve(userId, null);

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void givenExistingProjectForUser_whenResolve_thenReturnProjectRuntimeContext() {
        //given
        final Long userId = 17L;
        final UUID projectId = UUID.fromString("0bf2eeb7-3683-4f2f-ac53-b7b36e2e7728");
        final AgentProject project = this.getAgentProject(projectId, "Project Atlas", "Reusable domain context");
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId))
                .thenReturn(Optional.of(project));

        //when
        final Optional<ProjectRuntimeContext> actual = this.projectRuntimeContextResolver.resolve(userId, projectId);

        //then
        assertThat(actual).contains(new ProjectRuntimeContext(projectId, "Project Atlas", "Reusable domain context"));
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
    }

    @Test
    void givenMissingProjectForUser_whenResolve_thenThrowAgentNotFoundException() {
        //given
        final Long userId = 17L;
        final UUID projectId = UUID.fromString("e3574f9d-f5ad-448c-a329-74257549257d");
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.projectRuntimeContextResolver.resolve(userId, projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation project not found");
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, userId);
    }

    private AgentProject getAgentProject(final UUID id, final String name, final String context) {
        return AgentProject.builder()
                .id(id)
                .ownerUserId(17L)
                .name(name)
                .description("Project description")
                .context(context)
                .createdAt(Instant.parse("2026-05-20T10:00:00Z"))
                .updatedAt(Instant.parse("2026-05-20T10:00:00Z"))
                .build();
    }
}
