package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentAccessDeniedException;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentChatExecution;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentChatExecutionImpl implements GetAgentChatExecution {

    private final ChatExecutionRepository chatExecutionRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public ChatExecution execute(final UUID agentId, final UUID executionId, final UUID conversationId) {
        final ChatExecution execution = this.chatExecutionRepository.findByAgentIdAndExecutionId(agentId, executionId)
                .orElseThrow(() -> new AgentNotFoundException("Execution not found"));
        final Long userId = this.authenticatedUserProvider.getUserId();
        if (!execution.getUserId().equals(userId)) {
            throw new AgentAccessDeniedException("Execution is not accessible");
        }
        if (conversationId != null && !conversationId.equals(execution.getConversationId())) {
            throw new AgentLifecycleTransitionException("Conversation consistency check failed");
        }
        final ConversationMessage assistantMessage = execution.getAssistantMessageId() == null
                ? null
                : this.conversationMessageRepository
                .findAllByConversationIdOrderByCreatedAtAsc(execution.getConversationId())
                .stream()
                .filter(message -> execution.getAssistantMessageId().equals(message.getId()))
                .findFirst()
                .orElse(null);
        return execution.toBuilder()
                .assistantMessage(assistantMessage)
                .build();
    }
}
