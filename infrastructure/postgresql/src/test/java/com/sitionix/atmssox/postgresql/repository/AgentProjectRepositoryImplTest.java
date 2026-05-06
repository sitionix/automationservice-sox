package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentProjectInfraMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentProjectRepositoryImplTest {

    private AgentProjectRepositoryImpl repository;

    @Mock
    private AgentProjectJpaRepository agentProjectJpaRepository;

    @Mock
    private AgentProjectInfraMapper agentProjectInfraMapper;

    @BeforeEach
    void setUp() {
        this.repository = new AgentProjectRepositoryImpl(this.agentProjectJpaRepository, this.agentProjectInfraMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentProjectJpaRepository, this.agentProjectInfraMapper);
    }

    @Test
    void givenProject_whenSave_thenReturnMappedProject() {
        //given
        final AgentProject given = mock(AgentProject.class);
        final AgentProjectEntity mapped = mock(AgentProjectEntity.class);
        final AgentProjectEntity persisted = mock(AgentProjectEntity.class);
        final AgentProject expected = mock(AgentProject.class);

        when(this.agentProjectInfraMapper.asAgentProjectEntity(given)).thenReturn(mapped);
        when(this.agentProjectJpaRepository.save(mapped)).thenReturn(persisted);
        when(this.agentProjectInfraMapper.asAgentProject(persisted)).thenReturn(expected);

        //when
        final AgentProject actual = this.repository.save(given);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentProjectInfraMapper).asAgentProjectEntity(given);
        verify(this.agentProjectJpaRepository).save(mapped);
        verify(this.agentProjectInfraMapper).asAgentProject(persisted);
    }

    @Test
    void givenOwnerPageAndSize_whenFindAllVisibleByOwnerUserId_thenReturnMappedPage() {
        //given
        final Long ownerUserId = 17L;
        final int page = 1;
        final int size = 20;
        final AgentProjectEntity firstEntity = mock(AgentProjectEntity.class);
        final AgentProjectEntity secondEntity = mock(AgentProjectEntity.class);
        final AgentProject firstProject = mock(AgentProject.class);
        final AgentProject secondProject = mock(AgentProject.class);
        final List<AgentProjectEntity> entities = List.of(firstEntity, secondEntity);
        final Page<AgentProjectEntity> jpaPage = new PageImpl<>(entities, PageRequest.of(page, size), 41);
        final Sort expectedSort = Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("projectId")
        );

        when(this.agentProjectJpaRepository.findByOwnerUserIdAndStatusIdNot(
                ownerUserId,
                AgentProjectStatus.DELETED.getId(),
                PageRequest.of(page, size, expectedSort)
        )).thenReturn(jpaPage);
        when(this.agentProjectInfraMapper.asAgentProject(firstEntity)).thenReturn(firstProject);
        when(this.agentProjectInfraMapper.asAgentProject(secondEntity)).thenReturn(secondProject);

        //when
        final AgentProjectsPage actual = this.repository.findAllVisibleByOwnerUserId(ownerUserId, page, size);

        //then
        assertThat(actual.items()).isEqualTo(List.of(firstProject, secondProject));
        assertThat(actual.page()).isEqualTo(page);
        assertThat(actual.size()).isEqualTo(size);
        assertThat(actual.hasNext()).isEqualTo(true);
        verify(this.agentProjectJpaRepository).findByOwnerUserIdAndStatusIdNot(
                ownerUserId,
                AgentProjectStatus.DELETED.getId(),
                PageRequest.of(page, size, expectedSort)
        );
        verify(this.agentProjectInfraMapper).asAgentProject(firstEntity);
        verify(this.agentProjectInfraMapper).asAgentProject(secondEntity);
    }
}
