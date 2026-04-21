package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    ChatAgentResponseDTO asChatAgentResponseDto(ChatAgentResponse src);

    AgentDTO asAgentDto(Agent src);

    List<AgentDTO> asAgentDtos(List<Agent> src);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default AgentsResponseDTO asAgentsResponseDto(final List<Agent> agents) {
        return AgentsResponseDTO.builder()
                .items(this.asAgentDtos(agents))
                .build();
    }
}
