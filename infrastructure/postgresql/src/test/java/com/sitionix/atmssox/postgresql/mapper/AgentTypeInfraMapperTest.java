package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.postgresql.entity.agent.AgentTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentTypeInfraMapperTest {

    private AgentTypeInfraMapper agentTypeInfraMapper;

    @BeforeEach
    void setUp() {
        this.agentTypeInfraMapper = new AgentTypeInfraMapperImpl();
    }

    @Test
    void givenAgentTypeEntity_whenAsType_thenReturnAgentType() {
        //given
        final AgentTypeEntity given = AgentTypeEntity.builder()
                .id(2L)
                .description("SYSTEM_RULE_ANALYZER")
                .build();

        //when
        final AgentType actual = this.agentTypeInfraMapper.asType(given);

        //then
        assertThat(actual).isEqualTo(AgentType.SYSTEM_RULE_ANALYZER);
    }

    @Test
    void givenAgentType_whenAsTypeEntity_thenReturnAgentTypeEntity() {
        //given
        final AgentType given = AgentType.USER;

        //when
        final AgentTypeEntity actual = this.agentTypeInfraMapper.asTypeEntity(given);

        //then
        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getDescription()).isEqualTo("USER");
    }

    @Test
    void givenNullTypeEntity_whenAsType_thenReturnNull() {
        //given
        final AgentTypeEntity given = null;

        //when
        final AgentType actual = this.agentTypeInfraMapper.asType(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenNullType_whenAsTypeEntity_thenReturnNull() {
        //given
        final AgentType given = null;

        //when
        final AgentTypeEntity actual = this.agentTypeInfraMapper.asTypeEntity(given);

        //then
        assertThat(actual).isNull();
    }
}
