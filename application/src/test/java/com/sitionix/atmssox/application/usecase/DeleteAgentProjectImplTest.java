package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAgentProjectImplTest {

    private DeleteAgentProjectImpl deleteAgentProject;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.deleteAgentProject = new DeleteAgentProjectImpl(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenVisibleProject_whenExecute_thenSaveDeletedProject() {
        //given
        final UUID projectId = UUID.fromString("43d47d2b-d8df-4e0f-9cde-7d18f4df2f1b");
        final AgentProject current = this.getProject(AgentProjectStatus.ACTIVE);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when
        this.deleteAgentProject.execute(projectId);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectRepository).save(captor.capture());
        final AgentProject actual = captor.getValue();
        assertThat(actual.getStatus()).isEqualTo(AgentProjectStatus.DELETED);
        assertThat(actual.getUpdatedAt()).isAfter(current.getUpdatedAt());
    }

    @Test
    void givenUnknownOrHiddenProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("35b3bcf7-a8cb-4ee6-b52c-1e0d46223203");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.deleteAgentProject.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    private AgentProject getProject(final AgentProjectStatus status) {
        return AgentProject.builder()
                .id(UUID.fromString("4905281f-0186-4b3a-a49d-7302f2e6088d"))
                .ownerUserId(17L)
                .name("Project")
                .description("Description")
                .status(status)
                .createdAt(Instant.parse("2026-05-01T10:15:30Z"))
                .updatedAt(Instant.parse("2026-05-01T10:15:30Z"))
                .build();
    }
}
