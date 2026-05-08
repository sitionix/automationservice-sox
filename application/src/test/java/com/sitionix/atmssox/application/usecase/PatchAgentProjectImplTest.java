package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
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
class PatchAgentProjectImplTest {

    private PatchAgentProjectImpl patchAgentProject;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.patchAgentProject = new PatchAgentProjectImpl(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenPatchNameAndDescription_whenExecute_thenSaveTrimmedValues() {
        //given
        final UUID projectId = UUID.fromString("de273c1c-78de-4062-bfb0-f3156838fce8");
        final AgentProject current = this.getProject("Old", "Old desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand("  New name  ", "  New desc  ", "  New context  ");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when
        this.patchAgentProject.execute(projectId, command);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectRepository).save(captor.capture());
        final AgentProject actual = captor.getValue();
        assertThat(actual.getName()).isEqualTo("New name");
        assertThat(actual.getDescription()).isEqualTo("New desc");
        assertThat(actual.getContext()).isEqualTo("New context");
        assertThat(actual.getUpdatedAt()).isAfter(current.getUpdatedAt());
    }

    @Test
    void givenPatchDescriptionOnlyWithBlankValue_whenExecute_thenSaveNullDescription() {
        //given
        final UUID projectId = UUID.fromString("39cd5a49-6277-4ac8-af89-4f38e2af2f3f");
        final AgentProject current = this.getProject("Project", "Old desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand(null, "   ", null);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when
        this.patchAgentProject.execute(projectId, command);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectRepository).save(captor.capture());
        final AgentProject actual = captor.getValue();
        assertThat(actual.getName()).isEqualTo("Project");
        assertThat(actual.getDescription()).isNull();
    }

    @Test
    void givenBlankName_whenExecute_thenThrowValidation() {
        //given
        final UUID projectId = UUID.fromString("f0768e07-c85f-4f3c-b4dd-ed97d5bc9ba9");
        final AgentProject current = this.getProject("Current", "Current desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand("   ", null, null);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when then
        assertThatThrownBy(() -> this.patchAgentProject.execute(projectId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Project name must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenNoPatchFields_whenExecute_thenThrowValidation() {
        //given
        final UUID projectId = UUID.fromString("e5423ca5-a650-4fdf-8ef8-94a54f80da4b");
        final AgentProject current = this.getProject("Current", "Current desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand(null, null, null);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when then
        assertThatThrownBy(() -> this.patchAgentProject.execute(projectId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("At least one field (name, description or context) must be provided");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenUnknownOrHiddenProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("5aa5fcfe-9a51-4f1d-b2fb-e9ce270bf16a");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand("Name", null, null);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.patchAgentProject.execute(projectId, command))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenBlankContext_whenExecute_thenSaveNullContext() {
        //given
        final UUID projectId = UUID.fromString("a76674a6-4950-4450-bf39-ef0aa5827157");
        final AgentProject current = this.getProject("Project", "Old desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand(null, null, "   ");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when
        this.patchAgentProject.execute(projectId, command);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectRepository).save(captor.capture());
        assertThat(captor.getValue().getContext()).isNull();
    }

    @Test
    void givenTooLongContext_whenExecute_thenThrowValidation() {
        //given
        final UUID projectId = UUID.fromString("893680d8-960a-43f5-ae52-d8f0a64ad669");
        final AgentProject current = this.getProject("Current", "Current desc");
        final PatchAgentProjectCommand command = new PatchAgentProjectCommand(null, null, "a".repeat(5001));
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(current));

        //when then
        assertThatThrownBy(() -> this.patchAgentProject.execute(projectId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Project context must be at most 5000 characters");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    private AgentProject getProject(final String name, final String description) {
        return AgentProject.builder()
                .id(UUID.fromString("11a44ec8-c579-49b9-a3ac-75fa66b6ca3c"))
                .ownerUserId(17L)
                .name(name)
                .description(description)
                .context("Existing context")
                .status(AgentProjectStatus.ACTIVE)
                .createdAt(Instant.parse("2026-05-01T10:15:30Z"))
                .updatedAt(Instant.parse("2026-05-01T10:15:30Z"))
                .build();
    }
}
