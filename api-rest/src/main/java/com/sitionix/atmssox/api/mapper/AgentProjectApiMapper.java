package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AgentProjectApiMapper {

    AgentProjectDTO asAgentProjectDto(AgentProject src);

    List<AgentProjectDTO> asAgentProjectDtos(List<AgentProject> src);

    AgentProjectsPageResponseDTO asAgentProjectsPageResponseDto(AgentProjectsPage src);

    PatchAgentProjectCommand asPatchAgentProjectCommand(PatchAgentProjectRequestDTO src);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
