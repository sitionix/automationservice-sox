package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProjectStatusInfraMapperTest {

    private AgentProjectStatusInfraMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AgentProjectStatusInfraMapperImpl();
    }

    @Test
    void givenNullStatusEntity_whenAsStatus_thenReturnNull() {
        //when
        final AgentProjectStatus actual = this.mapper.asStatus(null);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenValidStatusEntity_whenAsStatus_thenReturnMappedStatus() {
        //given
        final AgentProjectStatusEntity given = AgentProjectStatusEntity.builder()
                .id(2L)
                .description("ARCHIVED")
                .build();

        //when
        final AgentProjectStatus actual = this.mapper.asStatus(given);

        //then
        assertThat(actual).isEqualTo(AgentProjectStatus.ARCHIVED);
    }

    @Test
    void givenNullStatus_whenAsStatusEntity_thenReturnNull() {
        //when
        final AgentProjectStatusEntity actual = this.mapper.asStatusEntity(null);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenValidStatus_whenAsStatusEntity_thenReturnMappedStatusEntity() {
        //given
        final AgentProjectStatus given = AgentProjectStatus.DELETED;

        //when
        final AgentProjectStatusEntity actual = this.mapper.asStatusEntity(given);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(3L);
        assertThat(actual.getDescription()).isNull();
    }
}
