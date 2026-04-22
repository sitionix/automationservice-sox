package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.ChatAgent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatAgentImpl implements ChatAgent {

    private static final int TITLE_MAX_LENGTH = 80;

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public ChatAgentResponse execute(final UUID agentId, final ChatAgentCommand command) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final String message = this.normalizeMessage(command);

        final Conversation conversation;
        final List<ConversationParticipant> participants;
        if (command.getConversationId() == null) {
            conversation = this.createConversation(userId, message);
            participants = this.createParticipants(conversation.getId(), agentId, userId, conversation.getCreatedAt());
            this.conversationParticipantRepository.saveAll(participants);
        } else {
            conversation = this.conversationRepository.findActiveByIdAndUserId(command.getConversationId(), userId)
                    .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));
            participants = this.conversationParticipantRepository.findAllByConversationId(conversation.getId());
        }

        final ChatAgentCommand normalizedCommand = ChatAgentCommand.builder()
                .conversationId(conversation.getId())
                .message(message)
                .build();
        return conversation.getType().handle(conversation, participants, normalizedCommand, userId);
    }

    private Conversation createConversation(final Long userId, final String firstMessage) {
        final Instant now = Instant.now();
        return this.conversationRepository.save(Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(this.buildTitle(firstMessage))
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastMessageAt(now)
                .build());
    }

    private List<ConversationParticipant> createParticipants(final UUID conversationId,
                                                             final UUID agentId,
                                                             final Long userId,
                                                             final Instant joinedAt) {
        return List.of(
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.USER)
                        .participantId(String.valueOf(userId))
                        .joinedAt(joinedAt)
                        .build(),
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId(agentId.toString())
                        .joinedAt(joinedAt)
                        .build()
        );
    }

    private String buildTitle(final String firstMessage) {
        if (firstMessage.length() <= TITLE_MAX_LENGTH) {
            return firstMessage;
        }
        return firstMessage.substring(0, TITLE_MAX_LENGTH);
    }

    private String normalizeMessage(final ChatAgentCommand command) {
        if (command == null || command.getMessage() == null || command.getMessage().trim().isEmpty()) {
            throw new AgentValidationException("Message must not be blank");
        }
        return command.getMessage().trim();
    }
}
