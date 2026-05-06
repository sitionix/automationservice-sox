package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.api.AgentChatApi;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.usecase.GetAgentChatExecution;
import com.sitionix.atmssox.domain.usecase.SubmitAgentChatExecution;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AgentChatController implements AgentChatApi {

    private final SubmitAgentChatExecution submitAgentChatExecution;
    private final GetAgentChatExecution getAgentChatExecution;
    private final AgentApiMapper agentApiMapper;

    @Override
    public ResponseEntity<SubmitChatExecutionResponseDTO> submitAgentChatExecution(final UUID agentId,
                                                                                    @Valid final ChatAgentRequestDTO chatAgentRequestDTO,
                                                                                    final String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.agentApiMapper.asSubmitChatExecutionResponseDto(
                this.submitAgentChatExecution.execute(agentId, this.agentApiMapper.asChatAgentCommand(chatAgentRequestDTO), idempotencyKey)
        ));
    }

    @Override
    public ResponseEntity<SubmitChatExecutionResponseDTO> submitAgentChatExecutionByExecutionsPath(final UUID agentId,
                                                                                                    @Valid final ChatAgentRequestDTO chatAgentRequestDTO,
                                                                                                    final String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.agentApiMapper.asSubmitChatExecutionResponseDto(
                this.submitAgentChatExecution.execute(agentId, this.agentApiMapper.asChatAgentCommand(chatAgentRequestDTO), idempotencyKey)
        ));
    }

    @Override
    public ResponseEntity<ChatExecutionDTO> getAgentChatExecution(final UUID agentId, final UUID executionId, final UUID conversationId) {
        return ResponseEntity.ok(this.agentApiMapper.asChatExecutionDto(
                this.getAgentChatExecution.execute(agentId, executionId, conversationId)
        ));
    }
}
