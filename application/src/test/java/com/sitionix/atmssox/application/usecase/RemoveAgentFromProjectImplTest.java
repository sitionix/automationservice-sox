package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveAgentFromProjectImplTest {

    private RemoveAgentFromProjectImpl removeAgentFromProject;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.removeAgentFromProject = new RemoveAgentFromProjectImpl(this.agentProjectRepository, this.agentRepository, this.agentProjectMemberRepository,
                this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.agentRepository, this.agentProjectMemberRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenActiveMembership_whenExecute_thenSoftDeleteMembership() {
        //given
        final UUID projectId = UUID.fromString("4f660650-8823-4f79-9f8a-f773f0f6506f");
        final UUID agentId = UUID.fromString("414f55ae-9850-42f3-a84b-d4ece65495dc");
        final AgentProjectMember membership = this.getActiveMembership(projectId, agentId);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(mock(AgentProject.class)));
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(mock(Agent.class)));
        when(this.agentProjectMemberRepository.findActiveByProjectIdAndAgentId(projectId, agentId)).thenReturn(Optional.of(membership));

        //when
        this.removeAgentFromProject.execute(projectId, agentId);

        //then
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentProjectMemberRepository).findActiveByProjectIdAndAgentId(projectId, agentId);
        verify(this.agentProjectMemberRepository).save(any());
    }

    @Test
    void givenMissingMembership_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("4f660650-8823-4f79-9f8a-f773f0f6506f");
        final UUID agentId = UUID.fromString("414f55ae-9850-42f3-a84b-d4ece65495dc");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(mock(AgentProject.class)));
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(mock(Agent.class)));
        when(this.agentProjectMemberRepository.findActiveByProjectIdAndAgentId(projectId, agentId)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.removeAgentFromProject.execute(projectId, agentId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project member not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentProjectMemberRepository).findActiveByProjectIdAndAgentId(projectId, agentId);
    }

    private AgentProjectMember getActiveMembership(final UUID projectId, final UUID agentId) {
        return AgentProjectMember.builder()
                .membershipId(UUID.fromString("36ce817f-59cc-4686-87b8-854c6a2d0ab8"))
                .projectId(projectId)
                .agentId(agentId)
                .status(AgentProjectMemberStatus.ACTIVE)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }
}
