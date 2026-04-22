package com.sitionix.atmssox.postgresql.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationParticipantEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationParticipantJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationParticipantRepositoryImplTest {

    private ConversationParticipantRepositoryImpl conversationParticipantRepository;

    @Mock
    private ConversationParticipantJpaRepository conversationParticipantJpaRepository;

    @Mock
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        this.conversationParticipantRepository = new ConversationParticipantRepositoryImpl(this.conversationParticipantJpaRepository, this.entityManager);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.conversationParticipantJpaRepository, this.entityManager);
    }

    @Test
    void givenParticipants_whenSaveAll_thenPersistParticipantsWithConversationReference() {
        //given
        final UUID conversationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationParticipant userParticipant = this.getConversationParticipant(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                conversationId,
                ConversationParticipantType.USER,
                "17"
        );
        final ConversationParticipant agentParticipant = this.getConversationParticipant(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                conversationId,
                ConversationParticipantType.AGENT,
                "agent-1"
        );
        final ConversationEntity conversationRef = this.getConversationEntity(conversationId);

        when(this.entityManager.getReference(ConversationEntity.class, conversationId)).thenReturn(conversationRef);

        //when
        this.conversationParticipantRepository.saveAll(List.of(userParticipant, agentParticipant));

        //then
        verify(this.entityManager, times(2)).getReference(ConversationEntity.class, conversationId);
        final ArgumentCaptor<List<ConversationParticipantEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.conversationParticipantJpaRepository).saveAll(captor.capture());
        final List<ConversationParticipantEntity> actual = captor.getValue();
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getParticipantId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(actual.get(0).getConversation()).isEqualTo(conversationRef);
        assertThat(actual.get(0).getParticipantType()).isEqualTo(ConversationParticipantType.USER);
        assertThat(actual.get(0).getParticipantRef()).isEqualTo("17");
        assertThat(actual.get(1).getParticipantId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(actual.get(1).getConversation()).isEqualTo(conversationRef);
        assertThat(actual.get(1).getParticipantType()).isEqualTo(ConversationParticipantType.AGENT);
        assertThat(actual.get(1).getParticipantRef()).isEqualTo("agent-1");
    }

    @Test
    void givenConversationId_whenFindAllByConversationId_thenReturnMappedParticipants() {
        //given
        final UUID conversationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationEntity conversationEntity = this.getConversationEntity(conversationId);
        final ConversationParticipantEntity userParticipant = new ConversationParticipantEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                conversationEntity,
                ConversationParticipantType.USER,
                "17",
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final ConversationParticipantEntity agentParticipant = new ConversationParticipantEntity(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                conversationEntity,
                ConversationParticipantType.AGENT,
                "agent-1",
                Instant.parse("2026-04-21T10:00:10Z")
        );
        when(this.conversationParticipantJpaRepository.findAllByConversationConversationId(conversationId))
                .thenReturn(List.of(userParticipant, agentParticipant));

        //when
        final List<ConversationParticipant> actual = this.conversationParticipantRepository.findAllByConversationId(conversationId);

        //then
        assertThat(actual).isEqualTo(List.of(
                this.getConversationParticipant(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        conversationId,
                        ConversationParticipantType.USER,
                        "17"
                ),
                ConversationParticipant.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId("agent-1")
                        .joinedAt(Instant.parse("2026-04-21T10:00:10Z"))
                        .build()
        ));
        verify(this.conversationParticipantJpaRepository).findAllByConversationConversationId(conversationId);
    }

    private ConversationParticipant getConversationParticipant(
            final UUID participantId,
            final UUID conversationId,
            final ConversationParticipantType participantType,
            final String participantRef
    ) {
        return ConversationParticipant.builder()
                .id(participantId)
                .conversationId(conversationId)
                .participantType(participantType)
                .participantId(participantRef)
                .joinedAt(Instant.parse("2026-04-21T10:00:00Z"))
                .build();
    }

    private ConversationEntity getConversationEntity(final UUID conversationId) {
        final ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setConversationId(conversationId);
        return conversationEntity;
    }
}
