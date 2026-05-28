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
import java.util.Collections;
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


    default AgentProjectFlowResponseDTO asAgentProjectFlowResponseDto(final AgentProjectFlow src) {
        if (src == null) {
            return null;
        }
        return AgentProjectFlowResponseDTO.builder()
                .flowId(src.getFlowId())
                .nodes(this.asAgentProjectFlowNodeDtos(src.getNodes()))
                .edges(this.asAgentProjectFlowEdgeDtos(src.getEdges()))
                .build();
    }

    default AgentProjectFlowPaletteResponseDTO asAgentProjectFlowPaletteResponseDto(final AgentProjectFlowPalette src) {
        if (src == null) {
            return null;
        }
        return AgentProjectFlowPaletteResponseDTO.builder()
                .sources(this.asAgentProjectFlowPaletteSourceDtos(src.getSources()))
                .build();
    }

    default List<AgentProjectFlowNodeDTO> asAgentProjectFlowNodeDtos(final List<AgentProjectFlowNode> src) {
        if (src == null) {
            return List.of();
        }
        return src.stream().map(this::asAgentProjectFlowNodeDto).toList();
    }

    default List<AgentProjectFlowEdgeDTO> asAgentProjectFlowEdgeDtos(final List<AgentProjectFlowEdge> src) {
        if (src == null) {
            return List.of();
        }
        return src.stream().map(this::asAgentProjectFlowEdgeDto).toList();
    }

    default List<AgentProjectFlowPaletteSourceDTO> asAgentProjectFlowPaletteSourceDtos(final List<AgentProjectFlowPaletteSource> src) {
        if (src == null) {
            return List.of();
        }
        return src.stream().map(this::asAgentProjectFlowPaletteSourceDto).toList();
    }

    default AgentProjectFlowNodeDTO asAgentProjectFlowNodeDto(final AgentProjectFlowNode src) {
        if (src == null) {
            return null;
        }
        return AgentProjectFlowNodeDTO.builder()
                .id(src.getId())
                .nodeType(src.getNodeType())
                .referenceId(src.getReferenceId())
                .position(AgentProjectFlowNodePositionDTO.builder().x(src.getPositionX()).y(src.getPositionY()).build())
                .designStatus(src.getDesignStatus())
                .config(src.getConfig() == null ? Collections.emptyMap() : src.getConfig())
                .build();
    }

    default AgentProjectFlowEdgeDTO asAgentProjectFlowEdgeDto(final AgentProjectFlowEdge src) {
        if (src == null) {
            return null;
        }
        return AgentProjectFlowEdgeDTO.builder()
                .id(src.getId())
                .sourceNodeId(src.getSourceNodeId())
                .targetNodeId(src.getTargetNodeId())
                .edgeType(src.getEdgeType())
                .config(src.getConfig() == null ? Collections.emptyMap() : src.getConfig())
                .build();
    }

    default AgentProjectFlowPaletteSourceDTO asAgentProjectFlowPaletteSourceDto(final AgentProjectFlowPaletteSource src) {
        if (src == null) {
            return null;
        }
        return AgentProjectFlowPaletteSourceDTO.builder()
                .sourceType(src.getSourceType())
                .sourceId(src.getSourceId())
                .sourceName(src.getSourceName())
                .build();
    }

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
