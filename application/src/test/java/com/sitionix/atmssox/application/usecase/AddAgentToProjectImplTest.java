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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddAgentToProjectImplTest {

    private AddAgentToProjectImpl addAgentToProject;

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
        this.addAgentToProject = new AddAgentToProjectImpl(this.agentProjectRepository, this.agentRepository, this.agentProjectMemberRepository,
                this.authenticatedUserProvider);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectRepository, this.agentRepository, this.agentProjectMemberRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenExistingDeletedMembership_whenExecute_thenReactivateMembership() {
        //given
        final UUID projectId = UUID.fromString("db28384c-e833-43be-b95e-9026766ca519");
        final UUID agentId = UUID.fromString("16550e8c-8ea5-43ff-a569-85f8d08fb301");
        final AgentProject project = mock(AgentProject.class);
        final Agent agent = mock(Agent.class);
        final AgentProjectMember member = this.getDeletedMember(projectId, agentId);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentProjectMemberRepository.findByProjectIdAndAgentId(projectId, agentId)).thenReturn(Optional.of(member));
        when(this.agentProjectMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agent.getId()).thenReturn(agentId);

        //when
        this.addAgentToProject.execute(projectId, agentId);

        //then
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentProjectMemberRepository).findByProjectIdAndAgentId(projectId, agentId);
        verify(this.agentProjectMemberRepository).save(any());
        assertThat(true).isTrue();
    }

    @Test
    void givenUnknownProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("2f48db9a-424f-4cae-a8af-7164f67fd056");
        final UUID agentId = UUID.fromString("264e9fbf-274b-48f3-8062-18e67eb72f50");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> this.addAgentToProject.execute(projectId, agentId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    private AgentProjectMember getDeletedMember(final UUID projectId, final UUID agentId) {
        return AgentProjectMember.builder()
                .membershipId(UUID.fromString("63759742-a72e-4c94-a4e0-6fc8d5090c2e"))
                .projectId(projectId)
                .agentId(agentId)
                .status(AgentProjectMemberStatus.DELETED)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();
    }
}
