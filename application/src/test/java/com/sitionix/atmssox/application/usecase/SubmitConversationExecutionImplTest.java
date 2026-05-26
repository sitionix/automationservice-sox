package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitConversationExecutionImplTest {

    private SubmitConversationExecutionImpl submitConversationExecution;

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;
    @Mock
    private ConversationMessageRepository conversationMessageRepository;
    @Mock
    private ChatExecutionRepository chatExecutionRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        final ConversationExecutionProperties conversationExecutionProperties = new ConversationExecutionProperties();
        conversationExecutionProperties.setRuntimeDispatchEnabled(false);
        this.submitConversationExecution = new SubmitConversationExecutionImpl(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider,
                conversationExecutionProperties
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.chatExecutionRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenSingleActiveAgent_whenExecute_thenPersistDispatchSkippedExecution() {
        //given
        final UUID conversationId = UUID.fromString("dc948cb2-c5eb-4f1b-b7ad-c3a676cb9fce");
        final UUID agentId = UUID.fromString("7e3f0b49-4d4b-4d8f-b16a-f785e85589ea");
        final UUID messageId = UUID.fromString("6f8ad305-5f9d-49ce-939d-9796467a9aa3");
        final UUID executionId = UUID.fromString("45cc54b4-3f9e-4a16-9afd-cd07129ca5cb");
        final Conversation conversation = this.getConversation(conversationId, 17L, UUID.fromString("5366d56e-1e31-4ef8-8924-77f317f7f6a2"));
        final ConversationMessage persistedMessage = this.getUserMessage(messageId, conversationId, "hello");
        final ChatExecution persistedExecution = this.getExecution(executionId, conversationId, agentId, messageId, "hello", 17L);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L)).thenReturn(Optional.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId))
                .thenReturn(List.of(this.getActiveAgentParticipant(conversationId, agentId)));
        when(this.conversationMessageRepository.save(any(ConversationMessage.class))).thenReturn(persistedMessage);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(this.chatExecutionRepository.save(any(ChatExecution.class))).thenReturn(persistedExecution);

        //when
        final ChatExecution actual = this.submitConversationExecution.execute(conversationId, "  hello  ");

        //then
        assertThat(actual.getExecutionId()).isEqualTo(executionId);
        assertThat(actual.getStatus()).isEqualTo(ChatExecutionStatus.DISPATCH_SKIPPED);
        assertThat(actual.isIdempotencyReplayed()).isFalse();
        final ArgumentCaptor<ChatExecution> executionCaptor = ArgumentCaptor.forClass(ChatExecution.class);
        verify(this.chatExecutionRepository).save(executionCaptor.capture());
        final ChatExecution toSave = executionCaptor.getValue();
        assertThat(toSave.getStatus()).isEqualTo(ChatExecutionStatus.DISPATCH_SKIPPED);
        assertThat(toSave.getRequestMessage()).isEqualTo("hello");
        assertThat(toSave.getAgentId()).isEqualTo(agentId);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(this.conversationMessageRepository).save(any(ConversationMessage.class));
        verify(this.conversationRepository).save(any(Conversation.class));
    }

    @Test
    void givenMultipleActiveAgents_whenExecute_thenReturnDispatchSkippedWithoutExecutionId() {
        //given
        final UUID conversationId = UUID.fromString("dc948cb2-c5eb-4f1b-b7ad-c3a676cb9fce");
        final Conversation conversation = this.getConversation(conversationId, 17L, UUID.fromString("5366d56e-1e31-4ef8-8924-77f317f7f6a2"));
        final UUID firstAgentId = UUID.fromString("e8ce979b-2a8e-4525-a6fc-e20454515b10");
        final UUID secondAgentId = UUID.fromString("fdbe740a-4f5c-4f01-b394-21b8a4671525");
        final UUID messageId = UUID.fromString("6f8ad305-5f9d-49ce-939d-9796467a9aa3");
        final ConversationMessage persistedMessage = this.getUserMessage(messageId, conversationId, "hello");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L)).thenReturn(Optional.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId))
                .thenReturn(List.of(
                        this.getActiveAgentParticipant(conversationId, firstAgentId),
                        this.getActiveAgentParticipant(conversationId, secondAgentId)
                ));
        when(this.conversationMessageRepository.save(any(ConversationMessage.class))).thenReturn(persistedMessage);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        //when
        final ChatExecution actual = this.submitConversationExecution.execute(conversationId, "hello");

        //then
        assertThat(actual.getExecutionId()).isNull();
        assertThat(actual.getStatus()).isEqualTo(ChatExecutionStatus.DISPATCH_SKIPPED);
        assertThat(actual.getInputMessageId()).isEqualTo(messageId);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(this.conversationMessageRepository).save(any(ConversationMessage.class));
        verify(this.conversationRepository).save(any(Conversation.class));
        verifyNoInteractions(this.chatExecutionRepository);
    }

    @Test
    void givenBlankMessage_whenExecute_thenThrowValidationException() {
        //given
        final UUID conversationId = UUID.fromString("dc948cb2-c5eb-4f1b-b7ad-c3a676cb9fce");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.submitConversationExecution.execute(conversationId, "   "))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.chatExecutionRepository
        );
    }

    @Test
    void givenConversationWithoutProject_whenExecute_thenThrowNotFound() {
        //given
        final UUID conversationId = UUID.fromString("dc948cb2-c5eb-4f1b-b7ad-c3a676cb9fce");
        final Conversation conversation = this.getConversation(conversationId, 17L, null);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationRepository.findActiveByIdAndUserId(conversationId, 17L)).thenReturn(Optional.of(conversation));

        //when
        //then
        assertThatThrownBy(() -> this.submitConversationExecution.execute(conversationId, "hello"))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationRepository).findActiveByIdAndUserId(conversationId, 17L);
        verifyNoInteractions(this.conversationParticipantRepository, this.conversationMessageRepository, this.chatExecutionRepository);
    }

    private Conversation getConversation(final UUID conversationId, final Long userId, final UUID projectId) {
        return Conversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .userId(userId)
                .type(ConversationType.MULTI_AGENT)
                .status(ConversationStatus.ACTIVE)
                .title("title")
                .createdAt(Instant.parse("2026-05-10T10:00:00Z"))
                .updatedAt(Instant.parse("2026-05-10T10:00:00Z"))
                .lastMessageAt(Instant.parse("2026-05-10T10:00:00Z"))
                .build();
    }

    private ConversationParticipant getActiveAgentParticipant(final UUID conversationId, final UUID agentId) {
        return ConversationParticipant.builder()
                .id(UUID.fromString("6cc84cf7-f9ab-4568-b313-4f6545ab7a2d"))
                .conversationId(conversationId)
                .participantType(ConversationParticipantType.AGENT)
                .participantId(agentId.toString())
                .status(AgentStatus.ACTIVE)
                .joinedAt(Instant.parse("2026-05-10T10:00:00Z"))
                .build();
    }

    private ConversationMessage getUserMessage(final UUID messageId, final UUID conversationId, final String content) {
        return ConversationMessage.builder()
                .id(messageId)
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.USER)
                .authorId("17")
                .content(content)
                .createdAt(Instant.parse("2026-05-10T10:00:01Z"))
                .build();
    }

    private ChatExecution getExecution(final UUID executionId,
                                       final UUID conversationId,
                                       final UUID agentId,
                                       final UUID inputMessageId,
                                       final String message,
                                       final Long userId) {
        return ChatExecution.builder()
                .executionId(executionId)
                .conversationId(conversationId)
                .agentId(agentId)
                .userId(userId)
                .status(ChatExecutionStatus.DISPATCH_SKIPPED)
                .requestMessage(message)
                .inputMessageId(inputMessageId)
                .createdAt(Instant.parse("2026-05-10T10:00:01Z"))
                .completedAt(Instant.parse("2026-05-10T10:00:01Z"))
                .build();
    }
}
