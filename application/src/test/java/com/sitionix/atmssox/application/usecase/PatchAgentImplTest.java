package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.forge.security.server.user.ForgeUserClient;
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

@ExtendWith(MockitoExtension.class)
class PatchAgentImplTest {

    private PatchAgentImpl patchAgent;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ForgeUserClient forgeUserClient;

    @BeforeEach
    void setUp() {
        this.patchAgent = new PatchAgentImpl(this.agentRepository, this.forgeUserClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.forgeUserClient);
    }

    @Test
    void givenNameOnlyPatchCommand_whenExecute_thenUpdateOnlyName() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand("  Updated Name  ", null);

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.patchAgent.execute(givenAgentId, givenCommand);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand, times(2)).name();
        verify(givenCommand).description();
        verify(givenCommand).instruction();
        verify(this.agentRepository).save(agentCaptor.capture());
        verifyNoMoreInteractions(givenCommand);

        final Agent saved = agentCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(current.getId());
        assertThat(saved.getUserId()).isEqualTo(current.getUserId());
        assertThat(saved.getName()).isEqualTo("Updated Name");
        assertThat(saved.getDescription()).isEqualTo(current.getDescription());
        assertThat(saved.getStatus()).isEqualTo(current.getStatus());
        assertThat(saved.getCreatedAt()).isEqualTo(current.getCreatedAt());
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isAfter(current.getUpdatedAt());
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenDescriptionOnlyPatchCommand_whenExecute_thenUpdateOnlyDescription() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, "  Updated Description  ");

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.patchAgent.execute(givenAgentId, givenCommand);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand, times(2)).description();
        verify(givenCommand).instruction();
        verify(this.agentRepository).save(agentCaptor.capture());
        verifyNoMoreInteractions(givenCommand);

        final Agent saved = agentCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(current.getName());
        assertThat(saved.getDescription()).isEqualTo("Updated Description");
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenNameAndDescriptionPatchCommand_whenExecute_thenUpdateBothFields() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand("  Updated Name  ", "  Updated Description  ");

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.patchAgent.execute(givenAgentId, givenCommand);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand, times(2)).name();
        verify(givenCommand, times(2)).description();
        verify(givenCommand).instruction();
        verify(this.agentRepository).save(agentCaptor.capture());
        verifyNoMoreInteractions(givenCommand);

        final Agent saved = agentCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Updated Name");
        assertThat(saved.getDescription()).isEqualTo("Updated Description");
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenInstructionOnlyPatchCommand_whenExecute_thenUpdateOnlyInstruction() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description", "Current instruction");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, null, "  Updated instruction  ");

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.patchAgent.execute(givenAgentId, givenCommand);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand).description();
        verify(givenCommand, times(2)).instruction();
        verify(this.agentRepository).save(agentCaptor.capture());
        verifyNoMoreInteractions(givenCommand);

        final Agent saved = agentCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(current.getName());
        assertThat(saved.getDescription()).isEqualTo(current.getDescription());
        assertThat(saved.getInstruction()).isEqualTo("Updated instruction");
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenBlankInstructionPatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description", "Current instruction");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, null, "   ");

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent instruction must not be blank");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand).description();
        verify(givenCommand, times(2)).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenEmptyPatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, null);

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("At least one field (name, description or instruction) must be provided");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand).description();
        verify(givenCommand).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenBlankNamePatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand("   ", null);

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent name is required");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand, times(2)).name();
        verify(givenCommand).description();
        verify(givenCommand).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenDescriptionLongerThanOneHundredSixtyPatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, "a".repeat(161));

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent description must be between 1 and 160 characters");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand, times(2)).description();
        verify(givenCommand).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenNameLongerThanSixtyPatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand("a".repeat(61), null);

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent name must be between 1 and 60 characters");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand, times(2)).name();
        verify(givenCommand).description();
        verify(givenCommand).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenBlankDescriptionPatchCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent("Current Name", "Current Description");
        final PatchAgentCommand givenCommand = this.getPatchAgentCommand(null, "   ");

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Agent description must be between 1 and 160 characters");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verify(givenCommand).name();
        verify(givenCommand, times(2)).description();
        verify(givenCommand).instruction();
        verifyNoMoreInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenUnknownAgentId_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final PatchAgentCommand givenCommand = mock(PatchAgentCommand.class);

        when(this.forgeUserClient.getUserId()).thenReturn(7L);
        when(this.agentRepository.findVisibleByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");

        verify(this.forgeUserClient).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(givenAgentId, 7L);
        verifyNoInteractions(givenCommand);
        verifyNoMoreInteractions(this.agentRepository);
    }

    @Test
    void givenForgeUserClientThrows_whenExecute_thenThrowAuthenticationRequiredException() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final PatchAgentCommand givenCommand = mock(PatchAgentCommand.class);

        when(this.forgeUserClient.getUserId()).thenThrow(new RuntimeException("No auth context"));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgent.execute(givenAgentId, givenCommand))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");

        verify(this.forgeUserClient).getUserId();
        verifyNoInteractions(givenCommand);
        verifyNoInteractions(this.agentRepository);
    }

    private PatchAgentCommand getPatchAgentCommand(final String name, final String description) {
        return this.getPatchAgentCommand(name, description, null);
    }

    private PatchAgentCommand getPatchAgentCommand(final String name, final String description, final String instruction) {
        final PatchAgentCommand patchAgentCommand = mock(PatchAgentCommand.class);
        when(patchAgentCommand.name()).thenReturn(name);
        when(patchAgentCommand.description()).thenReturn(description);
        when(patchAgentCommand.instruction()).thenReturn(instruction);
        return patchAgentCommand;
    }

    private Agent getAgent(final String name, final String description) {
        return this.getAgent(name, description, null);
    }

    private Agent getAgent(final String name, final String description, final String instruction) {
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name(name)
                .description(description)
                .instruction(instruction)
                .status(AgentStatus.DRAFT)
                .createdAt(Instant.parse("2026-04-10T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-10T10:10:00Z"))
                .build();
    }
}
