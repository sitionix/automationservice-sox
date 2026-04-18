package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateAgentImplTest {

    private CreateAgentImpl createAgent;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ForgeUserClient forgeUserClient;

    @BeforeEach
    void setUp() {
        this.createAgent = new CreateAgentImpl(this.agentRepository, this.forgeUserClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.forgeUserClient);
    }

    @Test
    void givenValidCommand_whenExecute_thenSaveDraftAgentWithTrimmedFields() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithNameAndDescription("  My Agent  ", "  Useful description  ");

        when(this.forgeUserClient.getUserId()).thenReturn(17L);
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.createAgent.execute(given);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.agentRepository).save(agentCaptor.capture());
        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);

        final Agent saved = agentCaptor.getValue();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(17L);
        assertThat(saved.getName()).isEqualTo("My Agent");
        assertThat(saved.getDescription()).isEqualTo("Useful description");
        assertThat(saved.getStatus()).isEqualTo(AgentStatus.DRAFT);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenNameAndDescriptionAtMaxLength_whenExecute_thenSaveAgent() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithNameAndDescription("n".repeat(60), "d".repeat(160));

        when(this.forgeUserClient.getUserId()).thenReturn(17L);
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.createAgent.execute(given);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.agentRepository).save(agentCaptor.capture());
        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);
        assertThat(actual).isEqualTo(agentCaptor.getValue());
    }

    @Test
    void givenBlankName_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithName("   ");

        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent name is required");

        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    @Test
    void givenNullName_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithName(null);

        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent name is required");

        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    @Test
    void givenNameLongerThanSixty_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithName("a".repeat(61));

        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent name must be between 1 and 60 characters");

        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    @Test
    void givenNullDescription_whenExecute_thenSaveAgentWithNullDescription() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithNameAndDescription("Valid name", null);

        when(this.forgeUserClient.getUserId()).thenReturn(17L);
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.createAgent.execute(given);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.agentRepository).save(agentCaptor.capture());
        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);
        assertThat(agentCaptor.getValue().getDescription()).isNull();
        assertThat(actual).isEqualTo(agentCaptor.getValue());
    }

    @Test
    void givenBlankDescription_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithNameAndDescription("Valid name", "   ");

        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent description must be between 1 and 160 characters");

        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    @Test
    void givenDescriptionLongerThanOneHundredSixty_whenExecute_thenThrowValidationException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommandWithNameAndDescription("Valid name", "a".repeat(161));

        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent description must be between 1 and 160 characters");

        verify(this.forgeUserClient).getUserId();
        verify(given).name();
        verify(given).description();
        verifyNoMoreInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    @Test
    void givenForgeUserClientThrows_whenExecute_thenThrowAuthenticationRequiredException() {
        //given
        final CreateAgentCommand given = this.getCreateAgentCommand();

        when(this.forgeUserClient.getUserId()).thenThrow(new RuntimeException("No auth context"));

        //when
        //then
        assertThatThrownBy(() -> this.createAgent.execute(given))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");

        verify(this.forgeUserClient).getUserId();
        verifyNoInteractions(given);
        verifyNoInteractions(this.agentRepository);
    }

    private CreateAgentCommand getCreateAgentCommandWithNameAndDescription(final String name, final String description) {
        final CreateAgentCommand createAgentCommand = mock(CreateAgentCommand.class);
        when(createAgentCommand.name()).thenReturn(name);
        when(createAgentCommand.description()).thenReturn(description);
        return createAgentCommand;
    }

    private CreateAgentCommand getCreateAgentCommandWithName(final String name) {
        final CreateAgentCommand createAgentCommand = mock(CreateAgentCommand.class);
        when(createAgentCommand.name()).thenReturn(name);
        return createAgentCommand;
    }

    private CreateAgentCommand getCreateAgentCommand() {
        return mock(CreateAgentCommand.class);
    }
}
