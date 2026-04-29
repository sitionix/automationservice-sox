package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentAccessDeniedException;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.repository.ChatExecutionRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentChatExecutionImplTest {

    private GetAgentChatExecutionImpl getAgentChatExecution;

    @Mock
    private ChatExecutionRepository chatExecutionRepository;
    @Mock
    private ConversationMessageRepository conversationMessageRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentChatExecution = new GetAgentChatExecutionImpl(
                this.chatExecutionRepository,
                this.conversationMessageRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.chatExecutionRepository, this.conversationMessageRepository, this.authenticatedUserProvider);
    }

    @Test
    void givenExecutionWithAssistantMessageId_whenExecute_thenAttachAssistantMessage() {
        //given
        final UUID agentId = UUID.fromString("e84df5fd-2992-4945-80c1-2d7bfbf1e84a");
        final UUID executionId = UUID.fromString("6354b6f8-1db3-459b-8526-c651df9d1f2d");
        final UUID conversationId = UUID.fromString("fd5f50f4-9f80-4483-8d8e-f97ef96fc764");
        final UUID assistantMessageId = UUID.fromString("4d8a1ef9-c46f-4d6a-a828-540f73d2ec16");
        final ChatExecution execution = ChatExecution.builder()
                .executionId(executionId)
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(17L)
                .status(ChatExecutionStatus.COMPLETED)
                .assistantMessageId(assistantMessageId)
                .build();
        final ConversationMessage assistantMessage = ConversationMessage.builder()
                .id(assistantMessageId)
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.AGENT)
                .authorId(agentId.toString())
                .content("done")
                .createdAt(Instant.parse("2026-04-29T00:10:00Z"))
                .build();

        when(this.chatExecutionRepository.findByAgentIdAndExecutionId(agentId, executionId)).thenReturn(Optional.of(execution));
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(assistantMessage));

        //when
        final ChatExecution actual = this.getAgentChatExecution.execute(agentId, executionId, conversationId);

        //then
        assertThat(actual.getAssistantMessage()).isEqualTo(assistantMessage);
        verify(this.chatExecutionRepository).findByAgentIdAndExecutionId(agentId, executionId);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Test
    void givenForeignExecutionOwner_whenExecute_thenThrowAccessDenied() {
        //given
        final UUID agentId = UUID.fromString("e84df5fd-2992-4945-80c1-2d7bfbf1e84a");
        final UUID executionId = UUID.fromString("6354b6f8-1db3-459b-8526-c651df9d1f2d");
        final ChatExecution execution = ChatExecution.builder()
                .executionId(executionId)
                .agentId(agentId)
                .conversationId(UUID.fromString("fd5f50f4-9f80-4483-8d8e-f97ef96fc764"))
                .userId(99L)
                .status(ChatExecutionStatus.QUEUED)
                .build();

        when(this.chatExecutionRepository.findByAgentIdAndExecutionId(agentId, executionId)).thenReturn(Optional.of(execution));
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.getAgentChatExecution.execute(agentId, executionId, null))
                .isInstanceOf(AgentAccessDeniedException.class)
                .hasMessage("Execution is not accessible");
        verify(this.chatExecutionRepository).findByAgentIdAndExecutionId(agentId, executionId);
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.conversationMessageRepository);
    }

    @Test
    void givenConversationMismatch_whenExecute_thenThrowLifecycleTransitionException() {
        //given
        final UUID agentId = UUID.fromString("e84df5fd-2992-4945-80c1-2d7bfbf1e84a");
        final UUID executionId = UUID.fromString("6354b6f8-1db3-459b-8526-c651df9d1f2d");
        final UUID conversationId = UUID.fromString("fd5f50f4-9f80-4483-8d8e-f97ef96fc764");
        final ChatExecution execution = ChatExecution.builder()
                .executionId(executionId)
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(17L)
                .status(ChatExecutionStatus.QUEUED)
                .build();

        when(this.chatExecutionRepository.findByAgentIdAndExecutionId(agentId, executionId)).thenReturn(Optional.of(execution));
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);

        //when
        //then
        assertThatThrownBy(() -> this.getAgentChatExecution.execute(agentId, executionId, UUID.fromString("13ed4ba2-c41f-4754-b3f3-f80b0d49c1e1")))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("Conversation consistency check failed");
        verify(this.chatExecutionRepository).findByAgentIdAndExecutionId(agentId, executionId);
        verify(this.authenticatedUserProvider).getUserId();
        verifyNoInteractions(this.conversationMessageRepository);
    }
}
