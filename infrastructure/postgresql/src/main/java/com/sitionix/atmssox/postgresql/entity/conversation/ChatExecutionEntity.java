package com.sitionix.atmssox.postgresql.entity.conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_executions", indexes = {
        @Index(name = "idx_chat_executions_agent", columnList = "agent_id,execution_id"),
        @Index(name = "idx_chat_executions_conversation_idempotency", columnList = "conversation_id,idempotency_key")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatExecutionEntity {

    @Id
    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false, referencedColumnName = "id")
    private ChatExecutionStatusEntity status;

    @Column(name = "request_message", nullable = false, columnDefinition = "TEXT")
    private String requestMessage;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "input_message_id", nullable = false)
    private UUID inputMessageId;

    @Column(name = "assistant_message_id")
    private UUID assistantMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "failure_class_id", referencedColumnName = "id")
    private ChatExecutionFailureClassEntity failureClass;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "failure_retryable")
    private Boolean failureRetryable;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
