package com.sitionix.atmssox.postgresql.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.agent.AgentStatusEntity;
import com.sitionix.atmssox.postgresql.entity.agent.AgentTypeEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentInfraMapperTest {

    private AgentInfraMapper agentInfraMapper;

    @Mock
    private AgentStatusInfraMapper agentStatusInfraMapper;
    @Mock
    private AgentTypeInfraMapper agentTypeInfraMapper;

    @BeforeEach
    void setUp() {
        this.agentInfraMapper = new AgentInfraMapperImpl(this.agentStatusInfraMapper, this.agentTypeInfraMapper);
    }

    @Test
    void givenAgent_whenAsAgentEntity_thenReturnAgentEntity() {
        //given
        final Agent given = this.getAgent();
        final AgentEntity expected = this.getAgentEntity();

        when(this.agentStatusInfraMapper.asStatusEntity(given.getStatus())).thenReturn(expected.getStatus());
        when(this.agentTypeInfraMapper.asTypeEntity(given.getType())).thenReturn(expected.getType());

        //when
        final AgentEntity actual = this.agentInfraMapper.asAgentEntity(given);

        //then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void givenAgentEntity_whenAsAgent_thenReturnAgent() {
        //given
        final AgentEntity given = this.getAgentEntity();
        final Agent expected = this.getAgent();

        when(this.agentStatusInfraMapper.asStatus(given.getStatus())).thenReturn(expected.getStatus());
        when(this.agentTypeInfraMapper.asType(given.getType())).thenReturn(expected.getType());

        //when
        final Agent actual = this.agentInfraMapper.asAgent(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgent_whenAsAgentEntity_thenReturnNull() {
        //given
        final Agent given = null;

        //when
        final AgentEntity actual = this.agentInfraMapper.asAgentEntity(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenNullAgentEntity_whenAsAgent_thenReturnNull() {
        //given
        final AgentEntity given = null;

        //when
        final Agent actual = this.agentInfraMapper.asAgent(given);

        //then
        assertThat(actual).isNull();
    }

    private Agent getAgent() {
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(17L)
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(AgentStatus.DRAFT)
                .createdAt(Instant.parse("2026-01-10T10:15:30Z"))
                .updatedAt(Instant.parse("2026-01-10T10:20:30Z"))
                .build();
    }

    private AgentEntity getAgentEntity() {
        return new AgentEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                17L,
                "My agent",
                "My description",
                "My instruction",
                AgentTypeEntity.builder()
                        .id(1L)
                        .description(AgentType.USER.name())
                        .build(),
                AgentStatusEntity.builder()
                        .id(1L)
                        .description("DRAFT")
                        .build(),
                Instant.parse("2026-01-10T10:15:30Z"),
                Instant.parse("2026-01-10T10:20:30Z")
        );
    }
}
