package com.sitionix.atmssox.postgresql.entity.conversation;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(
        name = "conversation_messages",
        indexes = {
                @Index(name = "idx_conversation_messages_order", columnList = "conversation_id,created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageEntity {

    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, referencedColumnName = "conversation_id")
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 32)
    private ConversationParticipantType authorType;

    @Column(name = "author_id", nullable = false, length = 64)
    private String authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
