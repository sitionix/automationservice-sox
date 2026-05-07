package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentProjectApi;
import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.api.mapper.AgentProjectApiMapper;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.usecase.CreateAgentProject;
import com.sitionix.atmssox.domain.usecase.DeleteAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProject;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
import com.sitionix.atmssox.domain.usecase.PatchAgentProject;
import jakarta.validation.Valid;
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
}
