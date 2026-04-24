package com.sitionix.atmssox.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentRuleApiMapperTest {

    private AgentRuleApiMapper agentRuleApiMapper;

    @BeforeEach
    void setUp() {
        this.agentRuleApiMapper = new AgentRuleApiMapperImpl();
    }

    @Test
    void givenCreateAgentRuleRequestDto_whenAsCreateAgentRuleCommand_thenReturnCommand() {
        //given
        final CreateAgentRuleRequestDTO given = CreateAgentRuleRequestDTO.builder()
                .text("Always keep output explicit.")
                .build();
        final CreateAgentRuleCommand expected = new CreateAgentRuleCommand("Always keep output explicit.");

        //when
        final CreateAgentRuleCommand actual = this.agentRuleApiMapper.asCreateAgentRuleCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenPatchAgentRuleRequestDto_whenAsPatchAgentRuleCommand_thenReturnCommand() {
        //given
        final PatchAgentRuleRequestDTO given = PatchAgentRuleRequestDTO.builder()
                .text("Use deterministic steps.")
                .build();
        final PatchAgentRuleCommand expected = new PatchAgentRuleCommand("Use deterministic steps.");

        //when
        final PatchAgentRuleCommand actual = this.agentRuleApiMapper.asPatchAgentRuleCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgentRule_whenAsAgentRuleDto_thenReturnRuleDto() {
        //given
        final AgentRule given = this.getAgentRule("Do not skip validation.");
        final AgentRuleDTO expected = AgentRuleDTO.builder()
                .id(given.getId())
                .text("Do not skip validation.")
                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                .build();

        //when
        final AgentRuleDTO actual = this.agentRuleApiMapper.asAgentRuleDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgentRules_whenAsAgentRulesResponseDto_thenReturnWrappedItems() {
        //given
        final List<AgentRule> given = List.of(this.getAgentRule("Rule A"), this.getAgentRule("Rule B"));
        final AgentRulesResponseDTO expected = AgentRulesResponseDTO.builder()
                .items(List.of(
                        AgentRuleDTO.builder()
                                .id(given.get(0).getId())
                                .text("Rule A")
                                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                                .updatedAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                                .build(),
                        AgentRuleDTO.builder()
                                .id(given.get(1).getId())
                                .text("Rule B")
                                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                                .updatedAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                                .build()
                ))
                .build();

        //when
        final AgentRulesResponseDTO actual = this.agentRuleApiMapper.asAgentRulesResponseDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenDeleteAgentRuleResponse_whenAsDeleteAgentRuleResponseDto_thenReturnDeletedStatus() {
        //given
        final DeleteAgentRuleResponse given = DeleteAgentRuleResponse.builder()
                .status(AgentRuleStatus.DELETED)
                .build();

        //when
        final DeleteAgentRuleResponseDTO actual = this.agentRuleApiMapper.asDeleteAgentRuleResponseDto(given);

        //then
        assertThat(actual.getStatus()).isEqualTo(DeleteAgentRuleResponseDTO.StatusEnum.DELETED);
    }

    private AgentRule getAgentRule(final String text) {
        return AgentRule.builder()
                .id(UUID.randomUUID())
                .agentId(UUID.fromString("b7111111-1111-1111-1111-111111111111"))
                .text(text)
                .status(AgentRuleStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:00:00Z"))
                .build();
    }
}
