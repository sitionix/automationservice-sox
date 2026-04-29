package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
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
    @Mock private ContextOptimizerProperties contextOptimizerProperties;
    @Mock private AgentExecutionService agentExecutionService;
    @Mock private PostChatWorkflowDispatcher postChatWorkflowDispatcher;

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
                this.contextOptimizerProperties,
                this.agentExecutionService,
                this.postChatWorkflowDispatcher
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

    private ChatExecution getExecution(final UUID executionId, final ChatExecutionStatus status, final Instant startedAt) {
        return ChatExecution.builder()
                .executionId(executionId)
                .agentId(UUID.fromString("101de9b2-ecfd-42e6-9139-8fef8f13b37d"))
                .conversationId(UUID.fromString("6dc93208-4841-4934-aa99-b8d7cbf8cca8"))
                .userId(17L)
                .status(status)
                .requestMessage("hello")
                .createdAt(Instant.parse("2026-04-29T00:00:00Z"))
                .startedAt(startedAt)
                .build();
    }
}
