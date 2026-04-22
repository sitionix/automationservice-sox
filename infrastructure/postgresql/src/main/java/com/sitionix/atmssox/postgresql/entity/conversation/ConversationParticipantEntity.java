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
        name = "conversation_participants",
        indexes = {
                @Index(name = "idx_conversation_participants_conversation", columnList = "conversation_id"),
                @Index(name = "idx_conversation_participants_lookup", columnList = "participant_type,participant_ref,conversation_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationParticipantEntity {

    @Id
    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, referencedColumnName = "conversation_id")
    private ConversationEntity conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false, length = 32)
    private ConversationParticipantType participantType;

    @Column(name = "participant_ref", nullable = false, length = 64)
    private String participantRef;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}
