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
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationAuthorType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
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

    private final AgentRepository agentRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ConversationContextBuilder conversationContextBuilder;
    private final OpenAiChatClient openAiChatClient;

    @Override
    @Transactional
    public ChatAgentResponse execute(final UUID agentId, final ChatAgentCommand command) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final Agent agent = this.agentRepository.findVisibleByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        if (agent.getStatus() != AgentStatus.ACTIVE) {
            throw new AgentChatNotAllowedException("Only ACTIVE agent can execute chat");
        }

        final String message = this.normalizeMessage(command);
        final Conversation conversation = this.resolveConversation(command, agentId, userId, message);

        final ConversationMessage userMessage = this.conversationMessageRepository.save(this.buildUserMessage(conversation.getId(), userId, message));
        final List<ConversationMessage> history = this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.getId());

        final String instruction = this.normalizeInstruction(agent);
        final String contextPrompt = this.conversationContextBuilder.build(history);
        final String replyContent = this.openAiChatClient.execute(instruction, contextPrompt);

        final ConversationMessage reply = this.conversationMessageRepository.save(this.buildAgentMessage(conversation.getId(), agentId, replyContent));
        this.touchConversation(conversation, reply.getCreatedAt());

        return ChatAgentResponse.builder()
                .conversationId(conversation.getId())
                .reply(reply)
                .build();
    }

    private Conversation resolveConversation(final ChatAgentCommand command,
                                             final UUID agentId,
                                             final Long userId,
                                             final String firstMessage) {
        if (command.getConversationId() == null) {
            return this.createConversation(agentId, userId, firstMessage);
        }
        return this.conversationRepository.findActiveByIdAndUserIdAndAgentId(command.getConversationId(), userId, agentId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));
    }

    private Conversation createConversation(final UUID agentId,
                                            final Long userId,
                                            final String firstMessage) {
        final Instant now = Instant.now();
        final Conversation created = this.conversationRepository.save(Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(this.buildTitle(firstMessage))
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastMessageAt(now)
                .build());

        this.conversationParticipantRepository.saveAll(List.of(
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(created.getId())
                        .participantType(ConversationParticipantType.USER)
                        .participantId(String.valueOf(userId))
                        .joinedAt(now)
                        .build(),
                ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(created.getId())
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId(agentId.toString())
                        .joinedAt(now)
                        .build()
        ));

        return created;
    }

    private void touchConversation(final Conversation conversation, final Instant lastMessageAt) {
        this.conversationRepository.save(conversation.toBuilder()
                .updatedAt(lastMessageAt)
                .lastMessageAt(lastMessageAt)
                .build());
    }

    private ConversationMessage buildUserMessage(final UUID conversationId, final Long userId, final String message) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(ConversationAuthorType.USER)
                .authorId(String.valueOf(userId))
                .content(message)
                .createdAt(Instant.now())
                .build();
    }

    private ConversationMessage buildAgentMessage(final UUID conversationId, final UUID agentId, final String message) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(ConversationAuthorType.AGENT)
                .authorId(agentId.toString())
                .content(message)
                .createdAt(Instant.now())
                .build();
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

    private String normalizeInstruction(final Agent agent) {
        return agent.getInstruction() == null ? "" : agent.getInstruction().trim();
    }
}
