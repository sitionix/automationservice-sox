package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.repository.AgentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAgentImplTest {

    private DeleteAgentImpl deleteAgent;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.deleteAgent = new DeleteAgentImpl(this.agentRepository, this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenDraftAgent_whenExecute_thenSaveAgentWithDeletedStatus() {
        //given
        final UUID givenAgentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Agent current = this.getAgent(AgentStatus.DRAFT);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(7L);
        when(this.agentRepository.findByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));
        when(this.agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final Agent actual = this.deleteAgent.execute(givenAgentId);

        //then
        final ArgumentCaptor<Agent> agentCaptor = ArgumentCaptor.forClass(Agent.class);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findByIdAndUserId(givenAgentId, 7L);
        verify(this.agentRepository).save(agentCaptor.capture());

        final Agent saved = agentCaptor.getValue();
        assertThat(saved.getId()).isEqualTo(current.getId());
        assertThat(saved.getUserId()).isEqualTo(current.getUserId());
        assertThat(saved.getName()).isEqualTo(current.getName());
        assertThat(saved.getDescription()).isEqualTo(current.getDescription());
        assertThat(saved.getInstruction()).isEqualTo(current.getInstruction());
        assertThat(saved.getStatus()).isEqualTo(AgentStatus.DELETED);
        assertThat(saved.getCreatedAt()).isEqualTo(current.getCreatedAt());
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(current.getUpdatedAt());
        assertThat(actual).isEqualTo(saved);
    }

    @Test
    void givenAlreadyDeletedAgent_whenExecute_thenReturnCurrentAgentWithoutSave() {
        //given
        final UUID givenAgentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final Agent current = this.getAgent(AgentStatus.DELETED);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(7L);
        when(this.agentRepository.findByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.of(current));

        //when
        final Agent actual = this.deleteAgent.execute(givenAgentId);

        //then
        assertThat(actual).isEqualTo(current);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findByIdAndUserId(givenAgentId, 7L);
    }

    @Test
    void givenUnknownAgentId_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID givenAgentId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(7L);
        when(this.agentRepository.findByIdAndUserId(givenAgentId, 7L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.deleteAgent.execute(givenAgentId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");

        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findByIdAndUserId(givenAgentId, 7L);
    }

    @Test
    void givenNoAuthenticationContext_whenExecute_thenThrowAuthenticationRequiredException() {
        //given
        final UUID givenAgentId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(this.authenticatedUserProvider.getUserId()).thenThrow(new AuthenticationRequiredException("Authentication required"));

        //when
        //then
        assertThatThrownBy(() -> this.deleteAgent.execute(givenAgentId))
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");

        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.agentRepository);
    }

    private Agent getAgent(final AgentStatus status) {
        return Agent.builder()
                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .userId(7L)
                .name("Agent Name")
                .description("Agent Description")
                .instruction("Agent instruction")
                .status(status)
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T10:00:00Z"))
                .build();
    }
}
