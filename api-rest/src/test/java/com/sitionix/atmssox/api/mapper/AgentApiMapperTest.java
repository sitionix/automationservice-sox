package com.sitionix.atmssox.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app_afesox.atmssox.api_first.dto.Agent;
import com.app_afesox.atmssox.api_first.dto.AgentsResponse;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequest;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentApiMapperTest {

    private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentApiMapper = new AgentApiMapperImpl();
    }

    @Test
    void givenCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnCreateAgentCommand() {
        //given
        final CreateAgentRequest given = this.getCreateAgentRequestDto();
        final CreateAgentCommand expected = this.getCreateAgentCommand();

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnNull() {
        //given
        final CreateAgentRequest given = null;

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenAgent_whenAsAgentDto_thenReturnAgentDto() {
        //given
        final com.sitionix.atmssox.domain.model.Agent given = this.getDomainAgent();
        final Agent expected = this.getApiAgent();

        //when
        final Agent actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgent_whenAsAgentDto_thenReturnNull() {
        //given
        final com.sitionix.atmssox.domain.model.Agent given = null;

        //when
        final Agent actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenAgentWithNullStatus_whenAsAgentDto_thenReturnAgentDtoWithNullStatus() {
        //given
        final com.sitionix.atmssox.domain.model.Agent given = this.getDomainAgentWithNullStatus();
        final Agent expected = this.getApiAgentWithNullStatus();

        //when
        final Agent actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgentList_whenAsAgentsResponseDto_thenReturnAgentsResponseDto() {
        //given
        final List<com.sitionix.atmssox.domain.model.Agent> given = List.of(this.getDomainAgent());
        final AgentsResponse expected = this.getAgentsResponseDto();

        //when
        final AgentsResponse actual = this.agentApiMapper.asAgentsResponseDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgentList_whenAsAgentDtos_thenReturnNull() {
        //given
        final List<com.sitionix.atmssox.domain.model.Agent> given = null;

        //when
        final List<Agent> actual = this.agentApiMapper.asAgentDtos(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenNullInstant_whenMap_thenReturnNull() {
        //given
        final Instant given = null;

        //when
        final OffsetDateTime actual = this.agentApiMapper.map(given);

        //then
        assertThat(actual).isNull();
    }

    private CreateAgentRequest getCreateAgentRequestDto() {
        return CreateAgentRequest.builder()
                .name("My agent")
                .description("My description")
                .build();
    }

    private CreateAgentCommand getCreateAgentCommand() {
        return CreateAgentCommand.builder()
                .name("My agent")
                .description("My description")
                .build();
    }

    private com.sitionix.atmssox.domain.model.Agent getDomainAgent() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return com.sitionix.atmssox.domain.model.Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .status(AgentStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private Agent getApiAgent() {
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .status(Agent.StatusEnum.DRAFT)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private com.sitionix.atmssox.domain.model.Agent getDomainAgentWithNullStatus() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return com.sitionix.atmssox.domain.model.Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .status(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private Agent getApiAgentWithNullStatus() {
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .status(null)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private AgentsResponse getAgentsResponseDto() {
        return AgentsResponse.builder()
                .items(List.of(this.getApiAgent()))
                .build();
    }
}
