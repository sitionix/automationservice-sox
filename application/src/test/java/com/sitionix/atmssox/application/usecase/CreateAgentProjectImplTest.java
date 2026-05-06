package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAgentProjectImplTest {

    private CreateAgentProjectImpl createAgentProject;

    @Mock
    private AgentProjectRepository agentProjectRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.createAgentProject = new CreateAgentProjectImpl(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenValidCommand_whenExecute_thenSaveActiveProjectWithTrimmedFields() {
        //given
        final CreateAgentProjectCommand given = this.getCreateAgentProjectCommand("  Marketing  ", "  Campaigns  ");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.save(any(AgentProject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final AgentProject actual = this.createAgentProject.execute(given);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).save(captor.capture());
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);

        final AgentProject saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerUserId()).isEqualTo(17L);
        assertThat(saved.getName()).isEqualTo("Marketing");
        assertThat(saved.getDescription()).isEqualTo("Campaigns");
        assertThat(saved.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenBlankName_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentProjectCommand given = mock(CreateAgentProjectCommand.class);
        when(given.name()).thenReturn("   ");

        //when
        //then
        assertThatThrownBy(() -> this.createAgentProject.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Project name must not be blank");

        verify(given).name();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentProjectRepository);
        verifyNoInteractions(this.authenticatedUserProvider);
    }

    @Test
    void givenBlankDescription_whenExecute_thenSaveProjectWithNullDescription() {
        //given
        final CreateAgentProjectCommand given = this.getCreateAgentProjectCommand("Name", "   ");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.save(any(AgentProject.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final AgentProject actual = this.createAgentProject.execute(given);

        //then
        final ArgumentCaptor<AgentProject> captor = ArgumentCaptor.forClass(AgentProject.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).save(captor.capture());
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);
        assertThat(captor.getValue().getDescription()).isNull();
        assertThat(actual).isEqualTo(captor.getValue());
    }

    private CreateAgentProjectCommand getCreateAgentProjectCommand(final String name, final String description) {
        final CreateAgentProjectCommand command = mock(CreateAgentProjectCommand.class);
        when(command.name()).thenReturn(name);
        when(command.description()).thenReturn(description);
        return command;
    }
}
