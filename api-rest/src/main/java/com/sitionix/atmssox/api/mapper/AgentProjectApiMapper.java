package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AddAgentToProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentsResponseDTO;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.PatchAgentProjectCommand;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AgentProjectApiMapper {

    AgentProjectDTO asAgentProjectDto(AgentProject src);

    List<AgentProjectDTO> asAgentProjectDtos(List<AgentProject> src);

    AgentProjectsPageResponseDTO asAgentProjectsPageResponseDto(AgentProjectsPage src);

    PatchAgentProjectCommand asPatchAgentProjectCommand(PatchAgentProjectRequestDTO src);

    ProjectAgentResponseDTO asProjectAgentResponseDto(ProjectAgent src);

    List<ProjectAgentResponseDTO> asProjectAgentResponseDtos(List<ProjectAgent> src);

    default ProjectAgentsResponseDTO asProjectAgentsResponseDto(final List<ProjectAgent> src) {
        final ProjectAgentsResponseDTO dto = new ProjectAgentsResponseDTO();
        dto.setItems(this.asProjectAgentResponseDtos(src));
        return dto;
    }

    default UUID asAgentId(final AddAgentToProjectRequestDTO src) {
        return src == null ? null : src.getAgentId();
    }

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
