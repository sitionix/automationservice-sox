package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSuggestionAnalyzerAgentExecutionHandlerTest {

    private RuleSuggestionAnalyzerAgentExecutionHandler ruleSuggestionAnalyzerAgentExecutionHandler;

    @Mock
    private OpenAiChatClient openAiChatClient;

    @BeforeEach
    void setUp() {
        this.ruleSuggestionAnalyzerAgentExecutionHandler = new RuleSuggestionAnalyzerAgentExecutionHandler(this.openAiChatClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.openAiChatClient);
    }

    @Test
    void givenNoInput_whenSupportedContextType_thenReturnRuleSuggestionAnalysisContextClass() {
        //given

        //when
        final Class<RuleSuggestionAnalysisContext> actual = this.ruleSuggestionAnalyzerAgentExecutionHandler.supportedContextType();

        //then
        assertThat(actual).isEqualTo(RuleSuggestionAnalysisContext.class);
        verifyNoInteractions(this.openAiChatClient);
    }

    @Test
    void givenBlankInstruction_whenExecute_thenThrowAgentValidationException() {
        //given
        final Agent givenAgent = this.getAgent("  ");
        final RuleSuggestionAnalysisContext givenContext = this.getFullContext();

        //when
        //then
        assertThatThrownBy(() -> this.ruleSuggestionAnalyzerAgentExecutionHandler.execute(givenAgent, givenContext))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("System agent instruction is empty");
        verifyNoInteractions(this.openAiChatClient);
    }

    @Test
    void givenContextWithRulesAndMessages_whenExecute_thenExecuteOpenAiWithFormattedPrompt() {
        //given
        final Agent givenAgent = this.getAgent("  Analyze rules  ");
        final RuleSuggestionAnalysisContext givenContext = this.getFullContext();
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class))).thenReturn("{\"suggestions\":[]}");

        //when
        final String actual = this.ruleSuggestionAnalyzerAgentExecutionHandler.execute(givenAgent, givenContext);

        //then
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);
        assertThat(actual).isEqualTo("{\"suggestions\":[]}");
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        final OpenAiChatRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.instruction()).isEqualTo("Analyze rules");
        assertThat(actualRequest.input()).contains("Target agent instruction:");
        assertThat(actualRequest.input()).contains("Keep answers practical.");
        assertThat(actualRequest.input()).contains("Active rules:");
        assertThat(actualRequest.input()).contains("- Active rule: Do not leak secrets");
        assertThat(actualRequest.input()).contains("Pending rules:");
        assertThat(actualRequest.input()).contains("- Pending rule: Ask for clarification");
        assertThat(actualRequest.input()).contains("Rejected rules:");
        assertThat(actualRequest.input()).contains("- Rejected rule: Be verbose");
        assertThat(actualRequest.input()).contains("Conversation:");
        assertThat(actualRequest.input()).contains("USER: hello");
        assertThat(actualRequest.input()).contains("AGENT: hi");
        assertThat(actualRequest.input()).contains("Latest user message:");
        assertThat(actualRequest.input()).contains("please propose rules");
    }

    @Test
    void givenContextWithoutRulesAndMessages_whenExecute_thenFormatPromptWithNoneSections() {
        //given
        final Agent givenAgent = this.getAgent("Instruction");
        final RuleSuggestionAnalysisContext givenContext = this.getEmptyContext();
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class))).thenReturn("ok");

        //when
        final String actual = this.ruleSuggestionAnalyzerAgentExecutionHandler.execute(givenAgent, givenContext);

        //then
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);
        assertThat(actual).isEqualTo("ok");
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().input()).contains("Active rules:\n(none)");
        assertThat(requestCaptor.getValue().input()).contains("Pending rules:\n(none)");
        assertThat(requestCaptor.getValue().input()).contains("Rejected rules:\n(none)");
        assertThat(requestCaptor.getValue().input()).contains("Conversation:\n(none)");
    }

    private Agent getAgent(final String instruction) {
        return Agent.builder()
                .id(UUID.fromString("138cfd84-b4f7-4362-a12e-1b714954f3f3"))
                .userId(17L)
                .name("Analyzer")
                .description("Analyzer description")
                .instruction(instruction)
                .type(AgentType.SYSTEM_RULE_ANALYZER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private RuleSuggestionAnalysisContext getFullContext() {
        final Agent targetAgent = Agent.builder()
                .id(UUID.fromString("f7eb0555-140f-4a5e-a8fa-02f95bfa4a2a"))
                .userId(17L)
                .name("Target")
                .description("Target description")
                .instruction("  Keep answers practical.  ")
                .type(AgentType.USER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
        return new RuleSuggestionAnalysisContext(
                targetAgent,
                List.of(this.getRule("Active rule", "Do not leak secrets", AgentRuleStatus.ACTIVE)),
                List.of(this.getRule("Pending rule", "Ask for clarification", AgentRuleStatus.PENDING)),
                List.of(this.getRule("Rejected rule", "Be verbose", AgentRuleStatus.REJECTED)),
                List.of(
                        this.getMessage(ConversationParticipantType.USER, "hello"),
                        this.getMessage(ConversationParticipantType.AGENT, "hi")
                ),
                "  please propose rules  "
        );
    }

    private RuleSuggestionAnalysisContext getEmptyContext() {
        final Agent targetAgent = Agent.builder()
                .id(UUID.fromString("69ea5f2f-5f4f-4565-ba65-7ee5f35f67ef"))
                .userId(17L)
                .name("Target")
                .description("Target description")
                .instruction(null)
                .type(AgentType.USER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
        return new RuleSuggestionAnalysisContext(
                targetAgent,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                " "
        );
    }

    private AgentRule getRule(final String title, final String content, final AgentRuleStatus status) {
        return AgentRule.builder()
                .id(UUID.randomUUID())
                .agentId(UUID.fromString("55c5f8bd-abec-4ab4-87f3-7b2529498588"))
                .title(title)
                .content(content)
                .status(status)
                .authorType(AgentRuleAuthorType.AI)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private ConversationMessage getMessage(final ConversationParticipantType authorType, final String content) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(UUID.fromString("0351b151-cf6d-4235-9bd3-e756ab6e1b65"))
                .authorType(authorType)
                .authorId(authorType == ConversationParticipantType.USER ? "17" : "agent")
                .content(content)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
