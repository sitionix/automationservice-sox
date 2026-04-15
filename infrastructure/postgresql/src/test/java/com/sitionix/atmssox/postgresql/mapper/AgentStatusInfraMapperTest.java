package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.postgresql.entity.agent.AgentStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentStatusInfraMapperTest {

    private AgentStatusInfraMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AgentStatusInfraMapperImpl();
    }

    @Test
    void givenNullStatusEntity_whenAsStatus_thenReturnNull() {
        // Given
        // When
        final AgentStatus result = mapper.asStatus(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenValidStatusEntity_whenAsStatus_thenReturnMappedStatus() {
        // Given
        final AgentStatusEntity statusEntity = AgentStatusEntity.builder()
                .id(1L)
                .description("DRAFT")
                .build();

        // When
        final AgentStatus result = mapper.asStatus(statusEntity);

        // Then
        assertThat(result).isEqualTo(AgentStatus.DRAFT);
    }

    @Test
    void givenNullStatus_whenAsStatusEntity_thenReturnNull() {
        // Given
        final AgentStatus status = null;

        // When
        final AgentStatusEntity result = mapper.asStatusEntity(status);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenValidStatus_whenAsStatusEntity_thenReturnMappedStatusEntity() {
        // Given
        final AgentStatus status = AgentStatus.ACTIVE;

        // When
        final AgentStatusEntity result = mapper.asStatusEntity(status);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getDescription()).isEqualTo("ACTIVE");
    }

}