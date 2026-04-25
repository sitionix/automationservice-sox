package com.sitionix.atmssox.postgresql.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentInfraMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentRepositoryImplTest {

    private AgentRepositoryImpl agentRepository;

    @Mock
    private AgentJpaRepository agentJpaRepository;

    @Mock
    private AgentInfraMapper agentInfraMapper;

    @BeforeEach
    void setUp() {
        this.agentRepository = new AgentRepositoryImpl(this.agentJpaRepository, this.agentInfraMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentJpaRepository, this.agentInfraMapper);
    }

    @Test
    void givenAgent_whenSave_thenReturnSavedAgent() {
        //given
        final Agent given = mock(Agent.class);
        final AgentEntity mappedEntity = mock(AgentEntity.class);
        final AgentEntity persistedEntity = mock(AgentEntity.class);
        final Agent expected = mock(Agent.class);

        when(this.agentInfraMapper.asAgentEntity(given)).thenReturn(mappedEntity);
        when(this.agentJpaRepository.save(mappedEntity)).thenReturn(persistedEntity);
        when(this.agentInfraMapper.asAgent(persistedEntity)).thenReturn(expected);

        //when
        final Agent actual = this.agentRepository.save(given);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentInfraMapper).asAgentEntity(given);
        verify(this.agentJpaRepository).save(mappedEntity);
        verify(this.agentInfraMapper).asAgent(persistedEntity);
    }

    @Test
    void givenUserId_whenFindAllVisibleByUserId_thenReturnMappedAgents() {
        //given
        final Long given = 17L;
        final AgentEntity firstEntity = mock(AgentEntity.class);
        final AgentEntity secondEntity = mock(AgentEntity.class);
        final Agent firstAgent = mock(Agent.class);
        final Agent secondAgent = mock(Agent.class);
        final List<Agent> expected = List.of(firstAgent, secondAgent);

        when(this.agentJpaRepository.findAllVisibleUserAgentsByUserIdOrderByUpdatedAtDesc(given, AgentStatus.DELETED.getId()))
                .thenReturn(List.of(firstEntity, secondEntity));
        when(this.agentInfraMapper.asAgent(firstEntity)).thenReturn(firstAgent);
        when(this.agentInfraMapper.asAgent(secondEntity)).thenReturn(secondAgent);

        //when
        final List<Agent> actual = this.agentRepository.findAllVisibleByUserId(given);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentJpaRepository).findAllVisibleUserAgentsByUserIdOrderByUpdatedAtDesc(
                given,
                AgentStatus.DELETED.getId()
        );
        verify(this.agentInfraMapper).asAgent(firstEntity);
        verify(this.agentInfraMapper).asAgent(secondEntity);
    }

    @Test
    void givenAgentIdAndUserId_whenFindByIdAndUserIdExists_thenReturnAgent() {
        //given
        final UUID agentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long userId = 17L;
        final AgentEntity entity = mock(AgentEntity.class);
        final Agent agent = mock(Agent.class);
        final Optional<Agent> expected = Optional.of(agent);

        when(this.agentJpaRepository.findUserAgentByAgentIdAndUserId(agentId, userId)).thenReturn(Optional.of(entity));
        when(this.agentInfraMapper.asAgent(entity)).thenReturn(agent);

        //when
        final Optional<Agent> actual = this.agentRepository.findByIdAndUserId(agentId, userId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentJpaRepository).findUserAgentByAgentIdAndUserId(agentId, userId);
        verify(this.agentInfraMapper).asAgent(entity);
    }

    @Test
    void givenAgentIdAndUserId_whenFindByIdAndUserIdMissing_thenReturnEmpty() {
        //given
        final UUID agentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long userId = 17L;
        final Optional<Agent> expected = Optional.empty();

        when(this.agentJpaRepository.findUserAgentByAgentIdAndUserId(agentId, userId)).thenReturn(Optional.empty());

        //when
        final Optional<Agent> actual = this.agentRepository.findByIdAndUserId(agentId, userId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentJpaRepository).findUserAgentByAgentIdAndUserId(agentId, userId);
    }
}
