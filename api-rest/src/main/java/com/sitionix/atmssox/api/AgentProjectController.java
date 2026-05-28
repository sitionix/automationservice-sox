package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentProjectApi;
import com.app_afesox.atmssox.api_first.dto.AddAgentToProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowPaletteResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectFlowResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentsResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentProjectApiMapper;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.usecase.AddAgentToProject;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import com.sitionix.atmssox.domain.usecase.DeleteAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectAgents;
import com.sitionix.atmssox.domain.usecase.GetAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectFlow;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectFlowPalette;
import com.sitionix.atmssox.domain.usecase.PatchAgentProject;
import com.sitionix.atmssox.domain.usecase.RemoveAgentFromProject;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentProjectController implements AgentProjectApi {

    private final CreateAgentProject createAgentProject;
    private final GetAgentProjects getAgentProjects;
    private final GetAgentProject getAgentProject;
    private final PatchAgentProject patchAgentProject;
    private final DeleteAgentProject deleteAgentProject;
    private final GetAgentProjectAgents getAgentProjectAgents;
    private final GetAgentProjectFlow getAgentProjectFlow;
    private final GetAgentProjectFlowPalette getAgentProjectFlowPalette;
    private final AddAgentToProject addAgentToProject;
    private final RemoveAgentFromProject removeAgentFromProject;
    private final AgentApiMapper agentApiMapper;
    private final AgentProjectApiMapper agentProjectApiMapper;

    @Override
    public ResponseEntity<AgentProjectDTO> createAgentProject(@Valid final CreateAgentProjectRequestDTO createAgentProjectRequestDTO) {
        final CreateAgentProjectCommand command = this.agentApiMapper.asCreateAgentProjectCommand(createAgentProjectRequestDTO);
        final AgentProject response = this.createAgentProject.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.agentProjectApiMapper.asAgentProjectDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectsPageResponseDTO> getAgentProjects(final Integer page, final Integer size) {
        final AgentProjectsPage response = this.getAgentProjects.execute(GetAgentProjectsQuery.builder().page(page).size(size).build());
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectsPageResponseDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectDTO> getAgentProject(final UUID projectId) {
        final AgentProject response = this.getAgentProject.execute(projectId);
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectDTO> patchAgentProject(final UUID projectId, @Valid final PatchAgentProjectRequestDTO patchAgentProjectRequestDTO) {
        final AgentProject response = this.patchAgentProject.execute(projectId, this.agentProjectApiMapper.asPatchAgentProjectCommand(patchAgentProjectRequestDTO));
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectDto(response));
    }

    @Override
    public ResponseEntity<Void> deleteAgentProject(final UUID projectId) {
        this.deleteAgentProject.execute(projectId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProjectAgentsResponseDTO> listAgentProjectAgents(final UUID projectId) {
        final List<ProjectAgent> response = this.getAgentProjectAgents.execute(projectId);
        return ResponseEntity.ok(this.agentProjectApiMapper.asProjectAgentsResponseDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectFlowResponseDTO> getAgentProjectFlow(final UUID projectId) {
        final AgentProjectFlow response = this.getAgentProjectFlow.execute(projectId);
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectFlowResponseDto(response));
    }

    @Override
    public ResponseEntity<AgentProjectFlowPaletteResponseDTO> getAgentProjectFlowPalette(final UUID projectId) {
        final AgentProjectFlowPalette response = this.getAgentProjectFlowPalette.execute(projectId);
        return ResponseEntity.ok(this.agentProjectApiMapper.asAgentProjectFlowPaletteResponseDto(response));
    }

    @Override
    public ResponseEntity<ProjectAgentResponseDTO> addAgentToProject(final UUID projectId,
                                                                     @Valid final AddAgentToProjectRequestDTO addAgentToProjectRequestDTO) {
        final UUID agentId = this.agentProjectApiMapper.asAgentId(addAgentToProjectRequestDTO);
        final ProjectAgent response = this.addAgentToProject.execute(projectId, agentId);
        return ResponseEntity.ok(this.agentProjectApiMapper.asProjectAgentResponseDto(response));
    }

    @Override
    public ResponseEntity<Void> removeAgentFromProject(final UUID projectId, final UUID agentId) {
        this.removeAgentFromProject.execute(projectId, agentId);
        return ResponseEntity.noContent().build();
    }
}
