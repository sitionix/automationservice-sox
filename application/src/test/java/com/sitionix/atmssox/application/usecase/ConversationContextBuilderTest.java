package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConversationContextBuilderTest {

    private ConversationContextBuilder conversationContextBuilder;

    @BeforeEach
    void setUp() {
        this.conversationContextBuilder = new ConversationContextBuilder();
    }

    @Test
    void givenOrderedHistory_whenBuild_thenReturnContextWithMessageAuthors() {
        //given
        final ConversationMessage userMessage = this.getConversationMessage(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                ConversationParticipantType.USER,
                "17",
                "Explain clean architecture",
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final ConversationMessage agentMessage = this.getConversationMessage(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                ConversationParticipantType.AGENT,
                "agent-1",
                "It separates core business logic from frameworks.",
                Instant.parse("2026-04-21T10:01:00Z")
        );
        final List<ConversationMessage> given = List.of(userMessage, agentMessage);
        final String expected = "Conversation history:\n"
                + "USER: Explain clean architecture\n"
                + "AGENT: It separates core business logic from frameworks.\n"
                + "Respond as AGENT to the latest USER message.";

        //when
        final String actual = this.conversationContextBuilder.build(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenEmptyOrderedHistory_whenBuild_thenReturnContextWithOnlyInstruction() {
        //given
        final List<ConversationMessage> given = List.of();
        final String expected = "Conversation history:\n"
                + "Respond as AGENT to the latest USER message.";

        //when
        final String actual = this.conversationContextBuilder.build(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    private ConversationMessage getConversationMessage(
            final UUID messageId,
            final ConversationParticipantType authorType,
            final String authorId,
            final String content,
            final Instant createdAt
    ) {
        return ConversationMessage.builder()
                .id(messageId)
                .conversationId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .authorType(authorType)
                .authorId(authorId)
                .content(content)
                .createdAt(createdAt)
                .build();
    }
}
