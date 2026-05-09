package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentConversationApi;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateProjectConversationRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationsResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import com.sitionix.atmssox.domain.usecase.CreateProjectConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import com.sitionix.atmssox.domain.usecase.GetProjectConversation;
import com.sitionix.atmssox.domain.usecase.ListProjectConversations;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentConversationController implements AgentConversationApi {

    private final GetAgentConversations getAgentConversations;
    private final GetAgentConversation getAgentConversation;
    private final DeleteAgentConversation deleteAgentConversation;
    private final CreateProjectConversation createProjectConversation;
    private final ListProjectConversations listProjectConversations;
    private final GetProjectConversation getProjectConversation;
    private final AgentApiMapper agentApiMapper;

    @Override
    public ResponseEntity<AgentConversationsResponseDTO> getAgentConversations(final UUID agentId) {
        return ResponseEntity.ok(this.agentApiMapper.asAgentConversationsResponseDto(this.getAgentConversations.execute(agentId)));
    }

    @Override
    public ResponseEntity<AgentConversationDetailsDTO> getAgentConversation(final UUID conversationId) {
        final ConversationDetails details = this.getAgentConversation.execute(conversationId);
        return ResponseEntity.ok(this.agentApiMapper.asAgentConversationDetailsDto(details));
    }

    @Override
    public ResponseEntity<Void> deleteAgentConversation(final UUID conversationId) {
        this.deleteAgentConversation.execute(conversationId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProjectConversationDetailsDTO> createProjectConversation(final UUID projectId,
                                                                                   @Valid final CreateProjectConversationRequestDTO createProjectConversationRequestDTO) {
        final CreateProjectConversationCommand command = this.agentApiMapper.asCreateProjectConversationCommand(createProjectConversationRequestDTO);
        final ProjectConversationDetails response = this.createProjectConversation.execute(projectId, command);
        return ResponseEntity.status(201).body(this.agentApiMapper.asProjectConversationDetailsDto(response));
    }

    @Override
    public ResponseEntity<ProjectConversationsResponseDTO> listProjectConversations(final UUID projectId) {
        return ResponseEntity.ok(this.agentApiMapper.asProjectConversationsResponseDto(this.listProjectConversations.execute(projectId)));
    }

    @Override
    public ResponseEntity<ProjectConversationDetailsDTO> getProjectConversation(final UUID projectId, final UUID conversationId) {
        return ResponseEntity.ok(this.agentApiMapper.asProjectConversationDetailsDto(this.getProjectConversation.execute(projectId, conversationId)));
    }
}
