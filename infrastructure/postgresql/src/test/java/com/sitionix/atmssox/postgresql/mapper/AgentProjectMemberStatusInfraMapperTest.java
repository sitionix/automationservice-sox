package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProjectMemberStatusInfraMapperTest {

    private AgentProjectMemberStatusInfraMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AgentProjectMemberStatusInfraMapperImpl();
    }

    @Test
    void givenNullStatusEntity_whenAsStatus_thenReturnNull() {
        //when
        final AgentProjectMemberStatus actual = this.mapper.asStatus(null);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenValidStatusEntity_whenAsStatus_thenReturnMappedStatus() {
        //given
        final AgentProjectMemberStatusEntity given = this.getStatusEntity(1L, "ACTIVE");

        //when
        final AgentProjectMemberStatus actual = this.mapper.asStatus(given);

        //then
        assertThat(actual).isEqualTo(AgentProjectMemberStatus.ACTIVE);
    }

    @Test
    void givenNullStatus_whenAsStatusEntity_thenReturnNull() {
        //when
        final AgentProjectMemberStatusEntity actual = this.mapper.asStatusEntity(null);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenValidStatus_whenAsStatusEntity_thenReturnMappedStatusEntity() {
        //given
        final AgentProjectMemberStatus given = AgentProjectMemberStatus.DELETED;

        //when
        final AgentProjectMemberStatusEntity actual = this.mapper.asStatusEntity(given);

        //then
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(2L);
        assertThat(actual.getDescription()).isNull();
    }

    private AgentProjectMemberStatusEntity getStatusEntity(final Long id, final String description) {
        return AgentProjectMemberStatusEntity.builder()
                .id(id)
                .description(description)
                .build();
    }
}
