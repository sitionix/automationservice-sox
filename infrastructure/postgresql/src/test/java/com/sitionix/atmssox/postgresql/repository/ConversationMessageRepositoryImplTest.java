package com.sitionix.atmssox.postgresql.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.jpa.ConversationMessageJpaRepository;
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
class ConversationMessageRepositoryImplTest {

    private ConversationMessageRepositoryImpl conversationMessageRepository;

    @Mock
    private ConversationMessageJpaRepository conversationMessageJpaRepository;

    @BeforeEach
    void setUp() {
        this.conversationMessageRepository = new ConversationMessageRepositoryImpl(this.conversationMessageJpaRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.conversationMessageJpaRepository);
    }

    @Test
    void givenConversationMessage_whenSave_thenReturnSavedMessage() {
        //given
        final UUID conversationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID messageId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final ConversationMessage given = this.getConversationMessage(messageId, conversationId);
        final ConversationEntity conversationRef = this.getConversationEntity(conversationId);
        final ConversationMessageEntity persistedEntity = this.getConversationMessageEntity(messageId, conversationRef);
        final ConversationMessage expected = this.getConversationMessage(messageId, conversationId);

        when(this.conversationMessageJpaRepository.save(any(ConversationMessageEntity.class))).thenReturn(persistedEntity);

        //when
        final ConversationMessage actual = this.conversationMessageRepository.save(given);

        //then
        assertThat(actual).isEqualTo(expected);
        final ArgumentCaptor<ConversationMessageEntity> captor = ArgumentCaptor.forClass(ConversationMessageEntity.class);
        verify(this.conversationMessageJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getMessageId()).isEqualTo(messageId);
        assertThat(captor.getValue().getConversation().getConversationId()).isEqualTo(conversationRef.getConversationId());
        assertThat(captor.getValue().getAuthorType()).isEqualTo(ConversationParticipantType.AGENT);
        assertThat(captor.getValue().getAuthorId()).isEqualTo("agent-1");
        assertThat(captor.getValue().getContent()).isEqualTo("Clean architecture separates concerns.");
    }

    @Test
    void givenConversationId_whenFindAllByConversationIdOrderByCreatedAtAsc_thenReturnMessages() {
        //given
        final UUID conversationId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final ConversationEntity firstConversationRef = this.getConversationEntity(conversationId);
        final ConversationEntity secondConversationRef = this.getConversationEntity(conversationId);
        final ConversationMessageEntity firstEntity =
                this.getConversationMessageEntity(UUID.fromString("11111111-1111-1111-1111-111111111111"), firstConversationRef);
        final ConversationMessageEntity secondEntity =
                this.getConversationMessageEntity(UUID.fromString("22222222-2222-2222-2222-222222222222"), secondConversationRef);
        final List<ConversationMessage> expected = List.of(
                this.getConversationMessage(UUID.fromString("11111111-1111-1111-1111-111111111111"), conversationId),
                this.getConversationMessage(UUID.fromString("22222222-2222-2222-2222-222222222222"), conversationId)
        );

        when(this.conversationMessageJpaRepository.findAllByConversationConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(firstEntity, secondEntity));

        //when
        final List<ConversationMessage> actual = this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.conversationMessageJpaRepository).findAllByConversationConversationIdOrderByCreatedAtAsc(conversationId);
    }

    private ConversationMessage getConversationMessage(final UUID messageId, final UUID conversationId) {
        return ConversationMessage.builder()
                .id(messageId)
                .conversationId(conversationId)
                .authorType(ConversationParticipantType.AGENT)
                .authorId("agent-1")
                .content("Clean architecture separates concerns.")
                .createdAt(Instant.parse("2026-04-21T10:01:00Z"))
                .build();
    }

    private ConversationMessageEntity getConversationMessageEntity(final UUID messageId, final ConversationEntity conversationRef) {
        return new ConversationMessageEntity(
                messageId,
                conversationRef,
                ConversationParticipantType.AGENT,
                "agent-1",
                "Clean architecture separates concerns.",
                Instant.parse("2026-04-21T10:01:00Z")
        );
    }

    private ConversationEntity getConversationEntity(final UUID conversationId) {
        final ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setConversationId(conversationId);
        return conversationEntity;
    }
}
