package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionFailureClassEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionStatusEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatExecutionInfraMapperTest {

    private ChatExecutionInfraMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChatExecutionInfraMapperImpl(
                new ChatExecutionStatusInfraMapperImpl(),
                new ChatExecutionFailureClassInfraMapperImpl()
        );
    }

    @Test
    void givenChatExecutionWithoutFailure_whenAsChatExecutionEntity_thenMapNullableFailureFieldsAsNull() {
        //given
        final ChatExecution source = this.getChatExecution(null);

        //when
        final ChatExecutionEntity actual = this.mapper.asChatExecutionEntity(source);

        //then
        assertThat(actual.getExecutionId()).isEqualTo(source.getExecutionId());
        assertThat(actual.getAgentId()).isEqualTo(source.getAgentId());
        assertThat(actual.getConversationId()).isEqualTo(source.getConversationId());
        assertThat(actual.getUserId()).isEqualTo(source.getUserId());
        assertThat(actual.getStatus().getId()).isEqualTo(source.getStatus().getId());
        assertThat(actual.getRequestMessage()).isEqualTo(source.getRequestMessage());
        assertThat(actual.getIdempotencyKey()).isEqualTo(source.getIdempotencyKey());
        assertThat(actual.getAssistantMessageId()).isEqualTo(source.getAssistantMessageId());
        assertThat(actual.getFailureClass()).isNull();
        assertThat(actual.getFailureReason()).isNull();
        assertThat(actual.getFailureRetryable()).isNull();
    }

    @Test
    void givenChatExecutionWithFailure_whenAsChatExecutionEntity_thenMapFailureFields() {
        //given
        final ChatExecutionFailure failure = ChatExecutionFailure.builder()
                .failureClass(ChatExecutionFailureClass.OWNERSHIP_VIOLATION)
                .reason("Access denied")
                .retryable(false)
                .build();
        final ChatExecution source = this.getChatExecution(failure);

        //when
        final ChatExecutionEntity actual = this.mapper.asChatExecutionEntity(source);

        //then
        assertThat(actual.getFailureClass().getId()).isEqualTo(1L);
        assertThat(actual.getFailureReason()).isEqualTo("Access denied");
        assertThat(actual.getFailureRetryable()).isFalse();
    }

    @Test
    void givenChatExecutionEntityWithoutFailureClass_whenAsChatExecution_thenKeepFailureNullAndIdempotencyReplayedFalse() {
        //given
        final ChatExecutionEntity source = this.getChatExecutionEntity(null, null, null);

        //when
        final ChatExecution actual = this.mapper.asChatExecution(source);

        //then
        assertThat(actual.getFailure()).isNull();
        assertThat(actual.isIdempotencyReplayed()).isFalse();
        assertThat(actual.getAssistantMessage()).isNull();
    }

    @Test
    void givenChatExecutionEntityWithFailureFields_whenAsChatExecution_thenMapFailure() {
        //given
        final ChatExecutionFailureClassEntity failureClass = ChatExecutionFailureClassEntity.builder()
                .id(5L)
                .description("EXECUTION_ERROR")
                .build();
        final ChatExecutionEntity source = this.getChatExecutionEntity(failureClass, "Boom", Boolean.TRUE);

        //when
        final ChatExecution actual = this.mapper.asChatExecution(source);

        //then
        assertThat(actual.getFailure()).isNotNull();
        assertThat(actual.getFailure().getFailureClass()).isEqualTo(ChatExecutionFailureClass.EXECUTION_ERROR);
        assertThat(actual.getFailure().getReason()).isEqualTo("Boom");
        assertThat(actual.getFailure().isRetryable()).isTrue();
    }

    @Test
    void givenNullRetryableFlag_whenAsChatExecution_thenMapFailureRetryableAsFalse() {
        //given
        final ChatExecutionFailureClassEntity failureClass = ChatExecutionFailureClassEntity.builder()
                .id(2L)
                .description("CONVERSATION_NOT_FOUND")
                .build();
        final ChatExecutionEntity source = this.getChatExecutionEntity(failureClass, "Missing", null);

        //when
        final ChatExecution actual = this.mapper.asChatExecution(source);

        //then
        assertThat(actual.getFailure()).isNotNull();
        assertThat(actual.getFailure().isRetryable()).isFalse();
    }

    private ChatExecution getChatExecution(final ChatExecutionFailure failure) {
        return ChatExecution.builder()
                .executionId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .agentId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .conversationId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                .userId(11L)
                .status(ChatExecutionStatus.QUEUED)
                .requestMessage("Explain architecture")
                .idempotencyKey("idem-1")
                .assistantMessageId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                .failure(failure)
                .createdAt(Instant.parse("2026-05-01T10:00:00Z"))
                .startedAt(Instant.parse("2026-05-01T10:00:01Z"))
                .completedAt(Instant.parse("2026-05-01T10:00:02Z"))
                .build();
    }

    private ChatExecutionEntity getChatExecutionEntity(
            final ChatExecutionFailureClassEntity failureClass,
            final String failureReason,
            final Boolean failureRetryable
    ) {
        return ChatExecutionEntity.builder()
                .executionId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .agentId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .conversationId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .userId(21L)
                .status(ChatExecutionStatusEntity.builder().id(4L).description("FAILED").build())
                .requestMessage("Request")
                .idempotencyKey("idem-2")
                .assistantMessageId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))
                .failureClass(failureClass)
                .failureReason(failureReason)
                .failureRetryable(failureRetryable)
                .createdAt(Instant.parse("2026-05-01T10:01:00Z"))
                .startedAt(Instant.parse("2026-05-01T10:01:01Z"))
                .completedAt(Instant.parse("2026-05-01T10:01:02Z"))
                .build();
    }
}
