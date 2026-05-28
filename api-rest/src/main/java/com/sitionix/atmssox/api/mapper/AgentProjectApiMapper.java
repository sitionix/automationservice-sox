package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowEdgeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowNodeDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowNodePositionDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowPaletteResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowPaletteSourceDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AddAgentToProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentsResponseDTO;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.model.AgentProjectFlowEdge;
import com.sitionix.atmssox.domain.model.AgentProjectFlowNode;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPaletteSource;
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
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AgentProjectApiMapper {

    AgentProjectDTO asAgentProjectDto(AgentProject src);

    List<AgentProjectDTO> asAgentProjectDtos(List<AgentProject> src);

    AgentProjectsPageResponseDTO asAgentProjectsPageResponseDto(AgentProjectsPage src);

    PatchAgentProjectCommand asPatchAgentProjectCommand(PatchAgentProjectRequestDTO src);

    ProjectAgentResponseDTO asProjectAgentResponseDto(ProjectAgent src);

    List<ProjectAgentResponseDTO> asProjectAgentResponseDtos(List<ProjectAgent> src);

    AgentProjectFlowResponseDTO asAgentProjectFlowResponseDto(AgentProjectFlow src);

    @Mapping(target = "position", expression = "java(this.mapPosition(src))")
    AgentProjectFlowNodeDTO asAgentProjectFlowNodeDto(AgentProjectFlowNode src);

    AgentProjectFlowEdgeDTO asAgentProjectFlowEdgeDto(AgentProjectFlowEdge src);

    AgentProjectFlowPaletteResponseDTO asAgentProjectFlowPaletteResponseDto(AgentProjectFlowPalette src);

    AgentProjectFlowPaletteSourceDTO asAgentProjectFlowPaletteSourceDto(AgentProjectFlowPaletteSource src);

    default ProjectAgentsResponseDTO asProjectAgentsResponseDto(final List<ProjectAgent> src) {
        final ProjectAgentsResponseDTO dto = new ProjectAgentsResponseDTO();
        dto.setItems(this.asProjectAgentResponseDtos(src));
        return dto;
    }

    default UUID asAgentId(final AddAgentToProjectRequestDTO src) {
        return src == null ? null : src.getAgentId();
    }

    default AgentProjectFlowNodePositionDTO mapPosition(final AgentProjectFlowNode src) {
        if (src == null || (src.getPositionX() == null && src.getPositionY() == null)) {
            return null;
        }
        return AgentProjectFlowNodePositionDTO.builder().x(src.getPositionX()).y(src.getPositionY()).build();
    }

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
