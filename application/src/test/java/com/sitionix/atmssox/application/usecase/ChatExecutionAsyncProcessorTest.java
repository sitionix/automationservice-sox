package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatExecutionAsyncProcessorTest {

    private ChatExecutionAsyncProcessor processor;

    @Mock private ChatExecutionRepository chatExecutionRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private ConversationMessageRepository conversationMessageRepository;
    @Mock private ConversationParticipantRepository conversationParticipantRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private AgentRuleRepository agentRuleRepository;
    @Mock private ConversationContextSnapshotRepository conversationContextSnapshotRepository;
    @Mock private ConversationContextBuilder conversationContextBuilder;
    @Mock private ProjectRuntimeContextResolver projectRuntimeContextResolver;
    @Mock private ContextOptimizerProperties contextOptimizerProperties;
    @Mock private AgentExecutionService agentExecutionService;
    @Mock private PostChatWorkflowDispatcher postChatWorkflowDispatcher;
    @Mock private ChatExecutionAsyncRunner chatExecutionAsyncRunner;

    @BeforeEach
    void setUp() {
        this.processor = new ChatExecutionAsyncProcessor(
                this.chatExecutionRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationParticipantRepository,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.projectRuntimeContextResolver,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher,
                this.chatExecutionAsyncRunner
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.chatExecutionRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationParticipantRepository,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.projectRuntimeContextResolver,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher,
                this.chatExecutionAsyncRunner
        );
    }

    @Test
    void givenSubmittedEvent_whenOnChatExecutionSubmitted_thenDelegateToAsyncRunner() {
        //given
        final UUID executionId = UUID.fromString("0fcef53d-2f54-4a12-8741-d4abf09ebaf3");

        //when
        this.processor.onChatExecutionSubmitted(new ChatExecutionSubmittedEvent(executionId));

        //then
        verify(this.chatExecutionAsyncRunner).processAsync(executionId);
        verifyNoInteractions(
                this.chatExecutionRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationParticipantRepository,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.projectRuntimeContextResolver,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher
        );
    }

    @Test
    void givenMissingQueuedExecution_whenProcess_thenReturnWithoutSideEffects() {
        //given
        final UUID executionId = UUID.fromString("0fcef53d-2f54-4a12-8741-d4abf09ebaf3");
        when(this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED)).thenReturn(Optional.empty());

        //when
        this.processor.process(executionId);

        //then
        verify(this.chatExecutionRepository).findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED);
        verifyNoInteractions(
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationParticipantRepository,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher
        );
    }

    @Test
    void givenConversationLookupFailure_whenProcess_thenPersistFailedExecution() {
        //given
        final UUID executionId = UUID.fromString("0fcef53d-2f54-4a12-8741-d4abf09ebaf3");
        final ChatExecution queued = this.getExecution(executionId, ChatExecutionStatus.QUEUED, null);
        final ChatExecution inProgress = this.getExecution(executionId, ChatExecutionStatus.IN_PROGRESS, null);

        when(this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED)).thenReturn(Optional.of(queued));
        when(this.chatExecutionRepository.save(any(ChatExecution.class))).thenReturn(inProgress);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId()))
                .thenThrow(new AgentNotFoundException("Conversation not found"));

        //when
        this.processor.process(executionId);

        //then
        final ArgumentCaptor<ChatExecution> executionCaptor = ArgumentCaptor.forClass(ChatExecution.class);
        verify(this.chatExecutionRepository, times(2)).save(executionCaptor.capture());
        final ChatExecution failedExecution = executionCaptor.getAllValues().get(1);
        assertThat(failedExecution.getStatus()).isEqualTo(ChatExecutionStatus.FAILED);
        assertThat(failedExecution.getFailure()).isNotNull();
        assertThat(failedExecution.getFailure().getFailureClass()).isEqualTo(ChatExecutionFailureClass.CONVERSATION_NOT_FOUND);
        assertThat(failedExecution.getFailure().getReason()).isEqualTo("Conversation not found");
        assertThat(failedExecution.getFailure().isRetryable()).isFalse();
        verify(this.chatExecutionRepository).findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId());
        verifyNoInteractions(
                this.conversationMessageRepository,
                this.conversationParticipantRepository,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher
        );
    }

    @Test
    void givenUnexpectedExecutionFailure_whenProcess_thenPersistRetryableExecutionError() {
        //given
        final UUID executionId = UUID.fromString("0fcef53d-2f54-4a12-8741-d4abf09ebaf3");
        final ChatExecution queued = this.getExecution(executionId, ChatExecutionStatus.QUEUED, null);
        final ChatExecution inProgress = this.getExecution(executionId, ChatExecutionStatus.IN_PROGRESS, null);
        final Conversation conversation = this.getConversation(queued.getConversationId(), queued.getUserId());
        final Agent agent = this.getAgent(queued.getAgentId(), queued.getUserId());

        when(this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED)).thenReturn(Optional.of(queued));
        when(this.chatExecutionRepository.save(any(ChatExecution.class))).thenReturn(inProgress);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId()))
                .thenReturn(Optional.of(conversation));
        when(this.conversationMessageRepository.findById(queued.getInputMessageId()))
                .thenReturn(Optional.of(this.getUserMessage(queued.getConversationId(), queued.getUserId(), queued.getRequestMessage())));
        when(this.conversationParticipantRepository.findAllByConversationId(conversation.getId())).thenReturn(List.of());
        when(this.agentRepository.findVisibleByIdAndUserId(queued.getAgentId(), queued.getUserId())).thenReturn(Optional.of(agent));
        when(this.projectRuntimeContextResolver.resolve(queued.getUserId(), null)).thenReturn(Optional.empty());
        when(this.agentExecutionService.execute(any(), any())).thenThrow(new IllegalStateException("gateway timeout"));

        //when
        this.processor.process(executionId);

        //then
        final ArgumentCaptor<ChatExecution> executionCaptor = ArgumentCaptor.forClass(ChatExecution.class);
        verify(this.chatExecutionRepository, times(2)).save(executionCaptor.capture());
        final ChatExecution failedExecution = executionCaptor.getAllValues().get(1);
        assertThat(failedExecution.getStatus()).isEqualTo(ChatExecutionStatus.FAILED);
        assertThat(failedExecution.getFailure()).isNotNull();
        assertThat(failedExecution.getFailure().getFailureClass()).isEqualTo(ChatExecutionFailureClass.EXECUTION_ERROR);
        assertThat(failedExecution.getFailure().getReason()).isEqualTo("Execution failed");
        assertThat(failedExecution.getFailure().isRetryable()).isTrue();
        verify(this.chatExecutionRepository).findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId());
        verify(this.conversationMessageRepository).findById(queued.getInputMessageId());
        verify(this.conversationParticipantRepository).findAllByConversationId(conversation.getId());
        verify(this.agentRepository).findVisibleByIdAndUserId(queued.getAgentId(), queued.getUserId());
        verify(this.conversationMessageRepository).findLastByConversationIdOrderByCreatedAtAsc(conversation.getId(), 0);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(
                queued.getAgentId(),
                queued.getUserId(),
                AgentRuleStatus.ACTIVE,
                null
        );
        verify(this.conversationContextSnapshotRepository).findByConversationId(conversation.getId());
        verify(this.projectRuntimeContextResolver).resolve(queued.getUserId(), null);
        verify(this.conversationContextBuilder).build(any(), any(), any(), any(), any(), any());
        verify(this.agentExecutionService).execute(any(), any());
        verify(this.contextOptimizerProperties).getLastMessagesLimit();
    }

    @Test
    void givenInactiveAgentConversation_whenProcess_thenPersistNonRetryableLifecycleFailure() {
        //given
        final UUID executionId = UUID.fromString("d2297bea-f56c-4e0e-a517-b5ddbec31482");
        final ChatExecution queued = this.getExecution(executionId, ChatExecutionStatus.QUEUED, null);
        final ChatExecution inProgress = this.getExecution(executionId, ChatExecutionStatus.IN_PROGRESS, null);
        final Conversation conversation = this.getConversation(queued.getConversationId(), queued.getUserId());
        final Agent inactiveAgent = this.getAgent(queued.getAgentId(), queued.getUserId()).toBuilder()
                .status(AgentStatus.DRAFT)
                .build();

        when(this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED)).thenReturn(Optional.of(queued));
        when(this.chatExecutionRepository.save(any(ChatExecution.class))).thenReturn(inProgress);
        when(this.conversationRepository.findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId()))
                .thenReturn(Optional.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversation.getId())).thenReturn(List.of());
        when(this.agentRepository.findVisibleByIdAndUserId(queued.getAgentId(), queued.getUserId())).thenReturn(Optional.of(inactiveAgent));

        //when
        this.processor.process(executionId);

        //then
        final ArgumentCaptor<ChatExecution> executionCaptor = ArgumentCaptor.forClass(ChatExecution.class);
        verify(this.chatExecutionRepository, times(2)).save(executionCaptor.capture());
        final ChatExecution failedExecution = executionCaptor.getAllValues().get(1);
        assertThat(failedExecution.getStatus()).isEqualTo(ChatExecutionStatus.FAILED);
        assertThat(failedExecution.getFailure()).isNotNull();
        assertThat(failedExecution.getFailure().getFailureClass()).isEqualTo(ChatExecutionFailureClass.INVALID_LIFECYCLE_STATE);
        assertThat(failedExecution.getFailure().getReason()).isEqualTo("Only ACTIVE agent can execute chat");
        assertThat(failedExecution.getFailure().isRetryable()).isFalse();
        verify(this.chatExecutionRepository).findByExecutionIdAndStatus(executionId, ChatExecutionStatus.QUEUED);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndAgentId(queued.getConversationId(), queued.getUserId(), queued.getAgentId());
        verify(this.conversationParticipantRepository).findAllByConversationId(conversation.getId());
        verify(this.agentRepository).findVisibleByIdAndUserId(queued.getAgentId(), queued.getUserId());
        verifyNoInteractions(
                this.agentRuleRepository,
                this.conversationContextSnapshotRepository,
                this.conversationContextBuilder,
                this.projectRuntimeContextResolver,
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher
        );
    }

    private Conversation getConversation(final UUID conversationId, final Long userId) {
        return Conversation.builder()
                .id(conversationId)
                .userId(userId)
                .type(ConversationType.DIRECT)
                .build();
    }

    private Agent getAgent(final UUID agentId, final Long userId) {
        return Agent.builder()
                .id(agentId)
                .userId(userId)
                .status(AgentStatus.ACTIVE)
                .instruction("instruction")
                .build();
    }

    private ChatExecution getExecution(final UUID executionId, final ChatExecutionStatus status, final Instant startedAt) {
        return ChatExecution.builder()
                .executionId(executionId)
                .agentId(UUID.fromString("101de9b2-ecfd-42e6-9139-8fef8f13b37d"))
                .conversationId(UUID.fromString("6dc93208-4841-4934-aa99-b8d7cbf8cca8"))
                .userId(17L)
                .status(status)
                .requestMessage("hello")
                .inputMessageId(UUID.fromString("96333f7d-c4db-44dc-9e04-dd6f3a0924da"))
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .startedAt(startedAt)
                .build();
    }

    private ConversationMessage getUserMessage(final UUID conversationId, final Long userId, final String content) {
        return ConversationMessage.builder()
                .id(UUID.fromString("96333f7d-c4db-44dc-9e04-dd6f3a0924da"))
                .conversationId(conversationId)
                .authorId(String.valueOf(userId))
                .content(content)
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .build();
    }
}
