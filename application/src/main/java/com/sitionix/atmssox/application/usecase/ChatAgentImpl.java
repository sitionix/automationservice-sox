package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.ChatAgent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatAgentImpl implements ChatAgent {

    private final AgentRepository agentRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final OpenAiChatClient openAiChatClient;

    @Override
    public ChatAgentResponse execute(final UUID agentId, final ChatAgentCommand command) {
        final Agent agent = this.agentRepository.findVisibleByIdAndUserId(agentId, this.authenticatedUserProvider.getUserId())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        if (agent.getStatus() != AgentStatus.ACTIVE) {
            throw new AgentChatNotAllowedException("Only ACTIVE agent can execute chat");
        }

        final String message = this.normalizeMessage(command);
        final String instruction = this.normalizeInstruction(agent);
        final String reply = this.openAiChatClient.execute(instruction, message);

        return ChatAgentResponse.builder()
                .reply(reply)
                .build();
    }

    private String normalizeMessage(final ChatAgentCommand command) {
        if (command == null || command.getMessage() == null || command.getMessage().trim().isEmpty()) {
            throw new AgentValidationException("Message must not be blank");
        }
        return command.getMessage().trim();
    }

    private String normalizeInstruction(final Agent agent) {
        return agent.getInstruction() == null ? "" : agent.getInstruction().trim();
    }
}
