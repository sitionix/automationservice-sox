package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentConversationApi;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
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
}
