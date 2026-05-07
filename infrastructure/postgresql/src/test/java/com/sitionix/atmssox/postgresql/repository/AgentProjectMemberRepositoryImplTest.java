package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectMemberJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentProjectMemberInfraMapper;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProjectMemberRepositoryImplTest {

    private AgentProjectMemberRepositoryImpl repository;

    @Mock
    private AgentProjectMemberJpaRepository agentProjectMemberJpaRepository;
    @Mock
    private AgentProjectMemberInfraMapper agentProjectMemberInfraMapper;

    @BeforeEach
    void setUp() {
        this.repository = new AgentProjectMemberRepositoryImpl(this.agentProjectMemberJpaRepository, this.agentProjectMemberInfraMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectMemberJpaRepository, this.agentProjectMemberInfraMapper);
    }

    @Test
    void givenMember_whenSave_thenReturnMappedMember() {
        //given
        final AgentProjectMember given = mock(AgentProjectMember.class);
        final AgentProjectMemberEntity mapped = mock(AgentProjectMemberEntity.class);
        final AgentProjectMemberEntity persisted = mock(AgentProjectMemberEntity.class);
        final AgentProjectMember expected = mock(AgentProjectMember.class);
        when(this.agentProjectMemberInfraMapper.asAgentProjectMemberEntity(given)).thenReturn(mapped);
        when(this.agentProjectMemberJpaRepository.save(mapped)).thenReturn(persisted);
        when(this.agentProjectMemberInfraMapper.asAgentProjectMember(persisted)).thenReturn(expected);

        //when
        final AgentProjectMember actual = this.repository.save(given);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentProjectMemberInfraMapper).asAgentProjectMemberEntity(given);
        verify(this.agentProjectMemberJpaRepository).save(mapped);
        verify(this.agentProjectMemberInfraMapper).asAgentProjectMember(persisted);
    }

    @Test
    void givenProjectAndAgentId_whenFindByProjectIdAndAgentId_thenReturnMappedMember() {
        //given
        final UUID projectId = UUID.fromString("2018995b-2ca6-42f7-bb07-3a6f8a892d4e");
        final UUID agentId = UUID.fromString("3ac392ee-6f41-450f-8f5d-bd5c5f3debb6");
        final AgentProjectMemberEntity entity = mock(AgentProjectMemberEntity.class);
        final AgentProjectMember expected = mock(AgentProjectMember.class);
        when(this.agentProjectMemberJpaRepository.findByProjectIdAndAgentId(projectId, agentId)).thenReturn(Optional.of(entity));
        when(this.agentProjectMemberInfraMapper.asAgentProjectMember(entity)).thenReturn(expected);

        //when
        final Optional<AgentProjectMember> actual = this.repository.findByProjectIdAndAgentId(projectId, agentId);

        //then
        assertThat(actual).contains(expected);
        verify(this.agentProjectMemberJpaRepository).findByProjectIdAndAgentId(projectId, agentId);
        verify(this.agentProjectMemberInfraMapper).asAgentProjectMember(entity);
    }

    @Test
    void givenProjectAndAgentId_whenFindActiveByProjectIdAndAgentId_thenReturnMappedMember() {
        //given
        final UUID projectId = UUID.fromString("2e760ac1-d8e0-458f-b8a7-8ef06421ff39");
        final UUID agentId = UUID.fromString("f0f5ad66-e3df-44dc-a6ea-364f1ff667f0");
        final AgentProjectMemberEntity entity = mock(AgentProjectMemberEntity.class);
        final AgentProjectMember expected = mock(AgentProjectMember.class);
        when(this.agentProjectMemberJpaRepository.findActiveByProjectIdAndAgentId(projectId, agentId, AgentProjectMemberStatus.ACTIVE.getId()))
                .thenReturn(Optional.of(entity));
        when(this.agentProjectMemberInfraMapper.asAgentProjectMember(entity)).thenReturn(expected);

        //when
        final Optional<AgentProjectMember> actual = this.repository.findActiveByProjectIdAndAgentId(projectId, agentId);

        //then
        assertThat(actual).contains(expected);
        verify(this.agentProjectMemberJpaRepository).findActiveByProjectIdAndAgentId(projectId, agentId, AgentProjectMemberStatus.ACTIVE.getId());
        verify(this.agentProjectMemberInfraMapper).asAgentProjectMember(entity);
    }

    @Test
    void givenVisibleMembers_whenFindVisibleProjectAgents_thenReturnMappedProjectAgents() {
        //given
        final UUID projectId = UUID.fromString("9f24ca55-a6e8-4747-b8ca-a6ac8a238f4b");
        final Long ownerUserId = 17L;
        final AgentProjectMemberEntity firstMemberEntity = mock(AgentProjectMemberEntity.class);
        final AgentProjectMemberEntity secondMemberEntity = mock(AgentProjectMemberEntity.class);
        final ProjectAgent firstProjectAgent = mock(ProjectAgent.class);
        final ProjectAgent secondProjectAgent = mock(ProjectAgent.class);
        when(this.agentProjectMemberJpaRepository.findVisibleProjectAgents(
                projectId,
                ownerUserId,
                3L,
                AgentStatus.DELETED.getId(),
                1L
        )).thenReturn(List.of(firstMemberEntity, secondMemberEntity));
        when(this.agentProjectMemberInfraMapper.asProjectAgent(firstMemberEntity)).thenReturn(firstProjectAgent);
        when(this.agentProjectMemberInfraMapper.asProjectAgent(secondMemberEntity)).thenReturn(secondProjectAgent);

        //when
        final List<ProjectAgent> actual = this.repository.findVisibleProjectAgents(projectId, ownerUserId);

        //then
        assertThat(actual).isEqualTo(List.of(firstProjectAgent, secondProjectAgent));
        verify(this.agentProjectMemberJpaRepository).findVisibleProjectAgents(
                projectId,
                ownerUserId,
                3L,
                AgentStatus.DELETED.getId(),
                1L
        );
        verify(this.agentProjectMemberInfraMapper).asProjectAgent(firstMemberEntity);
        verify(this.agentProjectMemberInfraMapper).asProjectAgent(secondMemberEntity);
    }
}
