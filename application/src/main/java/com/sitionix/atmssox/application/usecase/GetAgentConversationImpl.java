package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentConversationImpl implements GetAgentConversation {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ChatExecutionRepository chatExecutionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public ConversationDetails execute(final UUID conversationId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final Conversation conversation = this.conversationRepository.findActiveByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));
        final List<ConversationMessage> messages = this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        final List<ChatExecution> executions = this.chatExecutionRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);

        return ConversationDetails.builder()
                .conversation(conversation)
                .messages(messages)
                .executions(executions)
                .build();
    }
}
