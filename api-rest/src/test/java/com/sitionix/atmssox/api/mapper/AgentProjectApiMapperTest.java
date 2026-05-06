package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentProjectApiMapperTest {

    private AgentProjectApiMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new AgentProjectApiMapperImpl();
    }

    @Test
    void givenAgentProject_whenAsAgentProjectDto_thenReturnAgentProjectDto() {
        //given
        final AgentProject given = this.getAgentProject();

        //when
        final AgentProjectDTO actual = this.mapper.asAgentProjectDto(given);

        //then
        assertThat(actual.getId()).isEqualTo(UUID.fromString("f2f2b8c4-5039-4095-b5ec-d584bd429ca3"));
        assertThat(actual.getName()).isEqualTo("Project");
        assertThat(actual.getDescription()).isEqualTo("Description");
        assertThat(actual.getStatus().toString()).isEqualTo("ACTIVE");
        assertThat(actual.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-04-10T10:00:00Z"));
        assertThat(actual.getUpdatedAt()).isEqualTo(OffsetDateTime.parse("2026-04-10T10:01:00Z"));
    }

    @Test
    void givenAgentProjectsPage_whenAsAgentProjectsPageResponseDto_thenReturnPageResponseDto() {
        //given
        final AgentProjectsPage given = this.getAgentProjectsPage();

        //when
        final var actual = this.mapper.asAgentProjectsPageResponseDto(given);

        //then
        assertThat(actual.getItems()).hasSize(1);
        assertThat(actual.getPage()).isEqualTo(0);
        assertThat(actual.getSize()).isEqualTo(20);
        assertThat(actual.getHasNext()).isFalse();
    }

    private AgentProject getAgentProject() {
        return AgentProject.builder()
                .id(UUID.fromString("f2f2b8c4-5039-4095-b5ec-d584bd429ca3"))
                .ownerUserId(17L)
                .name("Project")
                .description("Description")
                .status(AgentProjectStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-10T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-10T10:01:00Z"))
                .build();
    }

    private AgentProjectsPage getAgentProjectsPage() {
        return AgentProjectsPage.builder()
                .items(List.of(this.getAgentProject()))
                .page(0)
                .size(20)
                .hasNext(false)
                .build();
    }
}
