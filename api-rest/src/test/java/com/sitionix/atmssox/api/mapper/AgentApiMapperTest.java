package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        final CreateAgentRequestDTO given = this.getCreateAgentRequestDto();
        final CreateAgentCommand expected = this.getCreateAgentCommand();

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnNull() {
        //given
        final CreateAgentRequestDTO given = null;

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenPatchAgentRequestDto_whenAsPatchAgentCommand_thenReturnPatchAgentCommand() {
        //given
        final PatchAgentRequestDTO given = this.getPatchAgentRequestDto();
        final PatchAgentCommand expected = this.getPatchAgentCommand();

        //when
        final PatchAgentCommand actual = this.agentApiMapper.asPatchAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullPatchAgentRequestDto_whenAsPatchAgentCommand_thenReturnNull() {
        //given
        final PatchAgentRequestDTO given = null;

        //when
        final PatchAgentCommand actual = this.agentApiMapper.asPatchAgentCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenAgent_whenAsAgentDto_thenReturnAgentDto() {
        //given
        final Agent given = this.getDomainAgent();
        final AgentDTO expected = this.getApiAgent();

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgent_whenAsAgentDto_thenReturnNull() {
        //given
        final Agent given = null;

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenAgentWithNullStatus_whenAsAgentDto_thenReturnAgentDtoWithNullStatus() {
        //given
        final Agent given = this.getDomainAgentWithNullStatus();
        final AgentDTO expected = this.getApiAgentWithNullStatus();

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgentList_whenAsAgentsResponseDto_thenReturnAgentsResponseDto() {
        //given
        final List<Agent> given = List.of(this.getDomainAgent());
        final AgentsResponseDTO expected = this.getAgentsResponseDto();

        //when
        final AgentsResponseDTO actual = this.agentApiMapper.asAgentsResponseDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgentList_whenAsAgentDtos_thenReturnNull() {
        //given
        final List<Agent> given = null;

        //when
        final List<AgentDTO> actual = this.agentApiMapper.asAgentDtos(given);

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

    private CreateAgentRequestDTO getCreateAgentRequestDto() {
        return CreateAgentRequestDTO.builder()
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

    private PatchAgentRequestDTO getPatchAgentRequestDto() {
        return PatchAgentRequestDTO.builder()
                .name("My patched agent")
                .description("My patched description")
                .build();
    }

    private PatchAgentCommand getPatchAgentCommand() {
        return PatchAgentCommand.builder()
                .name("My patched agent")
                .description("My patched description")
                .build();
    }

    private Agent getDomainAgent() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .status(AgentStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private AgentDTO getApiAgent() {
        return AgentDTO.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .status(AgentDTO.StatusEnum.DRAFT)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private Agent getDomainAgentWithNullStatus() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .status(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private AgentDTO getApiAgentWithNullStatus() {
        return AgentDTO.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .status(null)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private AgentsResponseDTO getAgentsResponseDto() {
        return AgentsResponseDTO.builder()
                .items(List.of(this.getApiAgent()))
                .build();
    }
}
