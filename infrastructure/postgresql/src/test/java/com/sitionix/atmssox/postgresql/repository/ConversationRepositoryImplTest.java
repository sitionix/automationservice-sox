package com.sitionix.atmssox.postgresql.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationJpaRepository;
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

@ExtendWith(MockitoExtension.class)
class ConversationRepositoryImplTest {

    private ConversationRepositoryImpl conversationRepository;

    @Mock
    private ConversationJpaRepository conversationJpaRepository;

    @BeforeEach
    void setUp() {
        this.conversationRepository = new ConversationRepositoryImpl(this.conversationJpaRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.conversationJpaRepository);
    }

    @Test
    void givenConversation_whenSave_thenReturnSavedConversation() {
        //given
        final UUID conversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Conversation given = this.getConversation(conversationId);
        final ConversationEntity persistedEntity = this.getConversationEntity(conversationId);
        final Conversation expected = this.getConversation(conversationId);

        when(this.conversationJpaRepository.save(any(ConversationEntity.class))).thenReturn(persistedEntity);

        //when
        final Conversation actual = this.conversationRepository.save(given);

        //then
        assertThat(actual).isEqualTo(expected);
        final ArgumentCaptor<ConversationEntity> captor = ArgumentCaptor.forClass(ConversationEntity.class);
        verify(this.conversationJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo(conversationId);
        assertThat(captor.getValue().getUserId()).isEqualTo(17L);
        assertThat(captor.getValue().getTitle()).isEqualTo("Explain clean architecture");
        assertThat(captor.getValue().getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(captor.getValue().getStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }

    @Test
    void givenConversationIdAndUserId_whenFindActiveByIdAndUserIdExists_thenReturnConversation() {
        //given
        final UUID conversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long userId = 17L;
        final ConversationEntity entity = this.getConversationEntity(conversationId);
        final Optional<Conversation> expected = Optional.of(this.getConversation(conversationId));

        when(this.conversationJpaRepository.findActiveByIdAndUserId(
                conversationId,
                ConversationStatus.ACTIVE,
                userId
        )).thenReturn(Optional.of(entity));

        //when
        final Optional<Conversation> actual = this.conversationRepository.findActiveByIdAndUserId(conversationId, userId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findActiveByIdAndUserId(
                conversationId,
                ConversationStatus.ACTIVE,
                userId
        );
    }

    @Test
    void givenConversationIdAndUserId_whenFindByIdAndUserIdExists_thenReturnConversation() {
        //given
        final UUID conversationId = UUID.fromString("12121212-1212-1212-1212-121212121212");
        final Long userId = 17L;
        final ConversationEntity entity = this.getConversationEntity(conversationId);
        final Optional<Conversation> expected = Optional.of(this.getConversation(conversationId));

        when(this.conversationJpaRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(entity));

        //when
        final Optional<Conversation> actual = this.conversationRepository.findByIdAndUserId(conversationId, userId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findByIdAndUserId(conversationId, userId);
    }

    @Test
    void givenConversationIdAndAgentId_whenFindActiveByIdAndAgentIdExists_thenReturnConversation() {
        //given
        final UUID conversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID agentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationEntity entity = this.getConversationEntity(conversationId);
        final Optional<Conversation> expected = Optional.of(this.getConversation(conversationId));

        when(this.conversationJpaRepository.findActiveByIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                ConversationParticipantType.AGENT,
                agentId.toString()
        )).thenReturn(Optional.of(entity));

        //when
        final Optional<Conversation> actual = this.conversationRepository.findActiveByIdAndAgentId(conversationId, agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findActiveByIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                ConversationParticipantType.AGENT,
                agentId.toString()
        );
    }

    @Test
    void givenConversationIdUserIdAndAgentId_whenFindActiveByIdAndUserIdAndAgentIdExists_thenReturnConversation() {
        //given
        final UUID conversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long userId = 17L;
        final UUID agentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationEntity entity = this.getConversationEntity(conversationId);
        final Optional<Conversation> expected = Optional.of(this.getConversation(conversationId));

        when(this.conversationJpaRepository.findActiveByIdAndUserIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        )).thenReturn(Optional.of(entity));

        //when
        final Optional<Conversation> actual = this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, userId, agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findActiveByIdAndUserIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        );
    }

    @Test
    void givenConversationIdUserIdAndAgentId_whenFindActiveByIdAndUserIdAndAgentIdMissing_thenReturnEmpty() {
        //given
        final UUID conversationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final Long userId = 17L;
        final UUID agentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final Optional<Conversation> expected = Optional.empty();

        when(this.conversationJpaRepository.findActiveByIdAndUserIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        )).thenReturn(Optional.empty());

        //when
        final Optional<Conversation> actual = this.conversationRepository.findActiveByIdAndUserIdAndAgentId(conversationId, userId, agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findActiveByIdAndUserIdAndAgent(
                conversationId,
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        );
    }

    @Test
    void givenUserIdAndAgentId_whenFindAllActiveByUserIdAndAgentId_thenReturnConversations() {
        //given
        final Long userId = 17L;
        final UUID agentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationEntity firstEntity = this.getConversationEntity(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        final ConversationEntity secondEntity = this.getConversationEntity(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        final List<Conversation> expected = List.of(
                this.getConversation(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                this.getConversation(UUID.fromString("22222222-2222-2222-2222-222222222222"))
        );

        when(this.conversationJpaRepository.findAllActiveByUserIdAndAgent(
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        )).thenReturn(List.of(firstEntity, secondEntity));

        //when
        final List<Conversation> actual = this.conversationRepository.findAllActiveByUserIdAndAgentId(userId, agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationJpaRepository).findAllActiveByUserIdAndAgent(
                ConversationStatus.ACTIVE,
                userId,
                ConversationParticipantType.AGENT,
                agentId.toString()
        );
    }

    private Conversation getConversation(final UUID conversationId) {
        return Conversation.builder()
                .id(conversationId)
                .userId(17L)
                .title("Explain clean architecture")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:01:00Z"))
                .lastMessageAt(Instant.parse("2026-04-21T10:01:00Z"))
                .build();
    }

    private ConversationEntity getConversationEntity(final UUID conversationId) {
        return new ConversationEntity(
                conversationId,
                17L,
                "Explain clean architecture",
                ConversationType.DIRECT,
                ConversationStatus.ACTIVE,
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:01:00Z"),
                Instant.parse("2026-04-21T10:01:00Z")
        );
    }
}
