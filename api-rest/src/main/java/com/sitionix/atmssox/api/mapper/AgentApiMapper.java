package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.Agent;
import com.app_afesox.atmssox.api_first.dto.AgentsResponse;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequest;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequest src);

    Agent asAgentDto(com.sitionix.atmssox.domain.model.Agent src);

    List<Agent> asAgentDtos(List<com.sitionix.atmssox.domain.model.Agent> src);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default AgentsResponse asAgentsResponseDto(final List<com.sitionix.atmssox.domain.model.Agent> agents) {
        return AgentsResponse.builder()
                .items(this.asAgentDtos(agents))
                .build();
    }
}
