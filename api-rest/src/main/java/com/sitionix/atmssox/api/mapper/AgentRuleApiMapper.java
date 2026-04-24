package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleAuthorTypeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO1;
import com.app_afesox.atmssox.api_first.dto.AgentRuleStatusDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.GetAgentRulesQuery;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentRuleApiMapper {

    CreateAgentRuleCommand asCreateAgentRuleCommand(CreateAgentRuleRequestDTO src);

    PatchAgentRuleCommand asPatchAgentRuleCommand(PatchAgentRuleRequestDTO src);

    AcceptAgentRuleCommand asAcceptAgentRuleCommand(AcceptAgentRuleRequestDTO src);

    AgentRuleDTO asAgentRuleDto(AgentRule src);

    AgentRuleDTO1 asAgentRuleDto1(AgentRule src);

    List<AgentRuleDTO> asAgentRuleDtos(List<AgentRule> src);

    List<AgentRuleDTO1> asAgentRuleDto1s(List<AgentRule> src);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default AgentRulesResponseDTO asAgentRulesResponseDto(final List<AgentRule> rules) {
        return new AgentRulesResponseDTO()
                .items(this.asAgentRuleDto1s(rules));
    }

    default GetAgentRulesQuery asGetAgentRulesQuery(final AgentRuleStatusDTO status, final AgentRuleAuthorTypeDTO authorType) {
        final AgentRuleStatus statusFilter = status == null ? AgentRuleStatus.ACTIVE : AgentRuleStatus.valueOf(status.getValue());
        final AgentRuleAuthorType authorTypeFilter = authorType == null ? null : AgentRuleAuthorType.valueOf(authorType.getValue());
        return new GetAgentRulesQuery(statusFilter, authorTypeFilter);
    }

    default DeleteAgentRuleResponseDTO asDeleteAgentRuleResponseDto(final DeleteAgentRuleResponse src) {
        return new DeleteAgentRuleResponseDTO()
                .status(DeleteAgentRuleResponseDTO.StatusEnum.DELETED);
    }
}
