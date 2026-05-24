package com.sitionix.atmssox.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
        final AgentRule rule = this.getAgentRule("Tone", "Always keep answers explicit.");
        final List<ConversationMessage> given = List.of(userMessage, agentMessage);
        final UserAgentExecutionContext expected = new UserAgentExecutionContext(
                """
                Follow architecture guidance.

                Active rules:
                - Always keep answers explicit.

                Conversation context summary:
                Project Alpha uses Spring Boot and Kafka.
                """.stripTrailing(),
                """
                Messages:
                USER: Explain clean architecture
                AGENT: It separates core business logic from frameworks.
                Respond as AGENT to the latest USER message.
                """.stripTrailing()
        );

        //when
        final UserAgentExecutionContext actual = this.conversationContextBuilder.build(
                "Follow architecture guidance.",
                List.of(rule),
                "Project Alpha uses Spring Boot and Kafka.",
                Optional.empty(),
                given,
                userMessage
        );

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenEmptyOrderedHistory_whenBuild_thenReturnContextWithOnlyInstruction() {
        //given
        final List<ConversationMessage> given = List.of();
        final ConversationMessage currentUserMessage = this.getConversationMessage(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                ConversationParticipantType.USER,
                "17",
                "Hello",
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final UserAgentExecutionContext expected = new UserAgentExecutionContext(
                "Instruction",
                """
                Messages:
                USER: Hello
                Respond as AGENT to the latest USER message.
                """.stripTrailing()
        );

        //when
        final UserAgentExecutionContext actual = this.conversationContextBuilder.build(
                "Instruction",
                List.of(),
                "",
                Optional.empty(),
                given,
                currentUserMessage
        );

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenProjectRuntimeContextWithDetails_whenBuild_thenPlaceProjectSectionBeforeMessages() {
        //given
        final ConversationMessage currentUserMessage = this.getConversationMessage(
                UUID.fromString("b31113f8-f718-44d1-a4e4-5284e5c3602a"),
                ConversationParticipantType.USER,
                "17",
                "What should we optimize first?",
                Instant.parse("2026-04-21T10:02:00Z")
        );
        final ConversationMessage previousMessage = this.getConversationMessage(
                UUID.fromString("a6f42af6-a3c4-4dbf-9778-dde9864e53c3"),
                ConversationParticipantType.AGENT,
                "agent-1",
                "Let's inspect execution latency trends.",
                Instant.parse("2026-04-21T10:01:00Z")
        );
        final Optional<ProjectRuntimeContext> projectRuntimeContext = Optional.of(new ProjectRuntimeContext(
                UUID.fromString("77e3cc4a-f43f-4ad7-9dd6-661f9586f717"),
                "Project Atlas",
                "Focus on deterministic context assembly."
        ));

        //when
        final UserAgentExecutionContext actual = this.conversationContextBuilder.build(
                "Instruction",
                List.of(),
                "",
                projectRuntimeContext,
                List.of(previousMessage),
                currentUserMessage
        );

        //then
        final String expectedInput = """
                Project context:
                Project name: Project Atlas
                Project details: Focus on deterministic context assembly.

                Messages:
                AGENT: Let's inspect execution latency trends.
                USER: What should we optimize first?
                Respond as AGENT to the latest USER message.
                """.stripTrailing();
        assertThat(actual.input()).isEqualTo(expectedInput);
    }

    @Test
    void givenProjectRuntimeContextWithoutDetails_whenBuild_thenUseFallbackProjectDetailsText() {
        //given
        final ConversationMessage currentUserMessage = this.getConversationMessage(
                UUID.fromString("8204348a-f7ed-4d50-aa8b-d4c11eb9f3e6"),
                ConversationParticipantType.USER,
                "17",
                "Summarize context",
                Instant.parse("2026-04-21T10:04:00Z")
        );
        final Optional<ProjectRuntimeContext> projectRuntimeContext = Optional.of(new ProjectRuntimeContext(
                UUID.fromString("66c779a0-81de-4294-bfdf-4f934bbaf4a5"),
                "Project Beta",
                "   "
        ));

        //when
        final UserAgentExecutionContext actual = this.conversationContextBuilder.build(
                "Instruction",
                List.of(),
                "",
                projectRuntimeContext,
                List.of(),
                currentUserMessage
        );

        //then
        assertThat(actual.input()).contains("Project details: No additional project context provided.");
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

    private AgentRule getAgentRule(final String title, final String content) {
        return AgentRule.builder()
                .id(UUID.fromString("3be0c922-c53e-4a0d-b17f-f6b6b9f63195"))
                .agentId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .title(title)
                .content(content)
                .status(AgentRuleStatus.ACTIVE)
                .authorType(AgentRuleAuthorType.USER)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:00:00Z"))
                .build();
    }
}
