package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAgentConversationImpl implements DeleteAgentConversation {

    private final ConversationRepository conversationRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public void execute(final UUID conversationId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final Conversation current = this.conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));

        if (current.getStatus() == ConversationStatus.DELETED) {
            return;
        }

        this.conversationRepository.save(current.toBuilder()
                .status(ConversationStatus.DELETED)
                .updatedAt(Instant.now())
                .build());
    }
}
