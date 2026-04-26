package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSuggestionAnalyzerAsyncServiceTest {

    private RuleSuggestionAnalyzerAsyncService ruleSuggestionAnalyzerAsyncService;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private AgentExecutionService agentExecutionService;

    @Mock
    private RuleSuggestionAnalyzerProperties properties;

    @Mock
    private ActiveUserAgentResolver activeUserAgentResolver;

    @BeforeEach
    void setUp() {
        this.ruleSuggestionAnalyzerAsyncService = new RuleSuggestionAnalyzerAsyncService(
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationMessageRepository,
                this.agentExecutionService,
                this.properties,
                new OpenAiJsonResponseParser(new ObjectMapper()),
                this.activeUserAgentResolver
        );
    }

    @Test
    void givenMissingAnalyzer_whenAnalyzeAsync_thenReturnWithoutFurtherCalls() {
        //given
        final UUID agentId = UUID.fromString("7fdf4da7-dd31-4738-b915-c1ecf11deabc");
        final UUID conversationId = UUID.fromString("0710f406-aa5f-4fbc-a116-7529a73f2ce5");
        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.empty());

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verifyNoInteractions(
                this.agentRuleRepository,
                this.conversationMessageRepository,
                this.agentExecutionService,
                this.properties
        );
    }

    @Test
    void givenTargetAgentNotActiveUser_whenAnalyzeAsync_thenReturnWithoutCallingAnalyzer() {
        //given
        final UUID agentId = UUID.fromString("79c5ae7d-f11d-4217-a6d6-6e65f0d28f6e");
        final UUID conversationId = UUID.fromString("ac3029ac-ff21-4eb7-a6a7-450aa16a79db");
        final Agent analyzer = this.getAgent(UUID.fromString("4f0fb6ec-5371-4b2f-bf2b-fefd42ccf31f"), AgentType.SYSTEM_RULE_ANALYZER, AgentStatus.ACTIVE, "Analyze");
        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.of(analyzer));
        when(this.activeUserAgentResolver.findById(agentId)).thenReturn(Optional.empty());

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verify(this.activeUserAgentResolver).findById(agentId);
        verifyNoInteractions(
                this.agentRuleRepository,
                this.conversationMessageRepository,
                this.agentExecutionService,
                this.properties
        );
    }

    @Test
    void givenNoLatestUserMessage_whenAnalyzeAsync_thenReturnWithoutSavingRules() {
        //given
        final UUID agentId = UUID.fromString("910e219e-a711-4de9-b618-419e6f46f26d");
        final UUID conversationId = UUID.fromString("f3e5298f-1cc3-4f3f-af42-4860ad7b1767");
        final Agent analyzer = this.getAgent(UUID.fromString("4f0fb6ec-5371-4b2f-bf2b-fefd42ccf31f"), AgentType.SYSTEM_RULE_ANALYZER, AgentStatus.ACTIVE, "Analyze");
        final ConversationMessage message = this.getMessage(conversationId, ConversationParticipantType.AGENT, "agent", "answer");

        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.of(analyzer));
        when(this.activeUserAgentResolver.findById(agentId)).thenReturn(Optional.of(
                this.getAgent(agentId, AgentType.USER, AgentStatus.ACTIVE, "Instruction")
        ));
        when(this.conversationMessageRepository.findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(Optional.empty());

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verify(this.activeUserAgentResolver).findById(agentId);
        verify(this.conversationMessageRepository).findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verifyNoInteractions(this.agentRuleRepository, this.agentExecutionService, this.properties);
    }

    @Test
    void givenAnalyzerExecutionFails_whenAnalyzeAsync_thenReturnWithoutSavingRules() {
        //given
        final UUID agentId = UUID.fromString("90af18a8-853e-4f34-8cea-a7f7060ea9c6");
        final UUID conversationId = UUID.fromString("f51f44e5-e6d0-438a-b467-5940ff412358");
        final Agent analyzer = this.getAgent(UUID.fromString("4f0fb6ec-5371-4b2f-bf2b-fefd42ccf31f"), AgentType.SYSTEM_RULE_ANALYZER, AgentStatus.ACTIVE, "Analyze");
        final Agent targetAgent = this.getAgent(agentId, AgentType.USER, AgentStatus.ACTIVE, "Instruction");
        final ConversationMessage userMessage = this.getMessage(conversationId, ConversationParticipantType.USER, "17", "Help");

        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.of(analyzer));
        when(this.activeUserAgentResolver.findById(agentId)).thenReturn(Optional.of(targetAgent));
        when(this.conversationMessageRepository.findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(Optional.of(userMessage));
        when(this.conversationMessageRepository.findLastByConversationIdOrderByCreatedAtAsc(conversationId, 20))
                .thenReturn(List.of(userMessage));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null))
                .thenReturn(List.of());
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null))
                .thenReturn(List.of());
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null))
                .thenReturn(List.of());
        when(this.properties.getLastMessagesLimit()).thenReturn(20);
        when(this.agentExecutionService.execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class)))
                .thenThrow(new OpenAiExecutionException("Analyzer OpenAI failure"));

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verify(this.activeUserAgentResolver).findById(agentId);
        verify(this.conversationMessageRepository).findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verify(this.conversationMessageRepository).findLastByConversationIdOrderByCreatedAtAsc(conversationId, 20);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null);
        verify(this.properties).getLastMessagesLimit();
        verify(this.agentExecutionService).execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class));
    }

    @Test
    void givenInvalidAnalyzerJsonResponse_whenAnalyzeAsync_thenReturnWithoutSavingRules() {
        //given
        final UUID agentId = UUID.fromString("962d4d67-6260-43e8-bf70-d4f1f57a7fd7");
        final UUID conversationId = UUID.fromString("41c82ca8-0650-4a8f-b2ea-a2f5b267905d");
        final Agent analyzer = this.getAgent(UUID.fromString("4f0fb6ec-5371-4b2f-bf2b-fefd42ccf31f"), AgentType.SYSTEM_RULE_ANALYZER, AgentStatus.ACTIVE, "Analyze");
        final Agent targetAgent = this.getAgent(agentId, AgentType.USER, AgentStatus.ACTIVE, "Instruction");
        final ConversationMessage userMessage = this.getMessage(conversationId, ConversationParticipantType.USER, "17", "Help");

        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.of(analyzer));
        when(this.activeUserAgentResolver.findById(agentId)).thenReturn(Optional.of(targetAgent));
        when(this.conversationMessageRepository.findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(Optional.of(userMessage));
        when(this.conversationMessageRepository.findLastByConversationIdOrderByCreatedAtAsc(conversationId, 20))
                .thenReturn(List.of(userMessage));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null))
                .thenReturn(List.of());
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null))
                .thenReturn(List.of());
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null))
                .thenReturn(List.of());
        when(this.properties.getLastMessagesLimit()).thenReturn(20);
        when(this.agentExecutionService.execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class))).thenReturn("{invalid");

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verify(this.activeUserAgentResolver).findById(agentId);
        verify(this.conversationMessageRepository).findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verify(this.conversationMessageRepository).findLastByConversationIdOrderByCreatedAtAsc(conversationId, 20);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null);
        verify(this.properties).getLastMessagesLimit();
        verify(this.agentExecutionService).execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class));
    }

    @Test
    void givenValidAnalyzerResponse_whenAnalyzeAsync_thenSaveNormalizedSuggestions() {
        //given
        final UUID agentId = UUID.fromString("561d1097-f1a8-4811-8497-07ef15354bd0");
        final UUID conversationId = UUID.fromString("2ad7366f-5f7f-4a00-9e0b-e65f4cdb44f8");
        final Agent analyzer = this.getAgent(UUID.fromString("4f0fb6ec-5371-4b2f-bf2b-fefd42ccf31f"), AgentType.SYSTEM_RULE_ANALYZER, AgentStatus.ACTIVE, "Analyze");
        final Agent targetAgent = this.getAgent(agentId, AgentType.USER, AgentStatus.ACTIVE, "Instruction");
        final ConversationMessage firstUserMessage = this.getMessage(conversationId, ConversationParticipantType.USER, "17", "First");
        final ConversationMessage agentMessage = this.getMessage(conversationId, ConversationParticipantType.AGENT, "agent", "Reply");
        final ConversationMessage latestUserMessage = this.getMessage(conversationId, ConversationParticipantType.USER, "17", "  latest ask  ");
        final AgentRule activeRule = this.getRule(agentId, "Active", "Focus on tests", AgentRuleStatus.ACTIVE);

        when(this.agentRepository.findSystemRuleAnalyzer()).thenReturn(Optional.of(analyzer));
        when(this.activeUserAgentResolver.findById(agentId)).thenReturn(Optional.of(targetAgent));
        when(this.conversationMessageRepository.findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(Optional.of(latestUserMessage));
        when(this.conversationMessageRepository.findLastByConversationIdOrderByCreatedAtAsc(conversationId, 2))
                .thenReturn(List.of(agentMessage, latestUserMessage));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null))
                .thenReturn(List.of(activeRule));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null))
                .thenReturn(List.of());
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null))
                .thenReturn(List.of());
        when(this.properties.getLastMessagesLimit()).thenReturn(2);
        when(this.properties.getMaxSuggestionContentLength()).thenReturn(20);
        when(this.properties.getMaxSuggestionsPerRun()).thenReturn(2);
        when(this.agentExecutionService.execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class)))
                .thenReturn("""
                        {
                          "suggestions": [
                            {"title": "  Rule 1  ", "content": "  Focus on logs  ", "reason": "  reason 1  "},
                            {"title": "Generic", "content": "be helpful", "reason": "generic reason"},
                            {"title": "Duplicate", "content": " focus on tests ", "reason": "r"},
                            {"title": "Too long", "content": "This rule is way too long for configured limits", "reason": "r"},
                            {"title": "No reason", "content": "Use IDs", "reason": " "},
                            {"title": "Rule 2", "content": "Use IDs", "reason": "reason 2"},
                            {"title": "Rule 3", "content": "Another", "reason": "reason 3"}
                          ]
                        }
                        """);
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        this.ruleSuggestionAnalyzerAsyncService.analyzeAsync(agentId, conversationId);

        //then
        final ArgumentCaptor<AgentRule> ruleCaptor = ArgumentCaptor.forClass(AgentRule.class);
        verify(this.agentRepository).findSystemRuleAnalyzer();
        verify(this.activeUserAgentResolver).findById(agentId);
        verify(this.conversationMessageRepository).findLastByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verify(this.conversationMessageRepository).findLastByConversationIdOrderByCreatedAtAsc(conversationId, 2);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.PENDING, null);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.REJECTED, null);
        verify(this.properties).getLastMessagesLimit();
        verify(this.properties, atLeastOnce()).getMaxSuggestionContentLength();
        verify(this.properties).getMaxSuggestionsPerRun();
        verify(this.agentExecutionService).execute(eq(analyzer), any(RuleSuggestionAnalysisContext.class));
        verify(this.agentRuleRepository, times(2)).save(ruleCaptor.capture());
        final List<AgentRule> savedRules = ruleCaptor.getAllValues();
        assertThat(savedRules).hasSize(2);
        assertThat(savedRules.get(0).getTitle()).isEqualTo("Rule 1");
        assertThat(savedRules.get(0).getContent()).isEqualTo("Focus on logs");
        assertThat(savedRules.get(0).getStatus()).isEqualTo(AgentRuleStatus.PENDING);
        assertThat(savedRules.get(0).getAuthorType()).isEqualTo(AgentRuleAuthorType.AI);
        assertThat(savedRules.get(1).getTitle()).isEqualTo("Generic");
        assertThat(savedRules.get(1).getContent()).isEqualTo("be helpful");
    }

    private Agent getAgent(final UUID id, final AgentType type, final AgentStatus status, final String instruction) {
        return Agent.builder()
                .id(id)
                .userId(17L)
                .name("Name")
                .description("Description")
                .instruction(instruction)
                .type(type)
                .status(status)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private AgentRule getRule(final UUID agentId, final String title, final String content, final AgentRuleStatus status) {
        return AgentRule.builder()
                .id(UUID.randomUUID())
                .agentId(agentId)
                .title(title)
                .content(content)
                .status(status)
                .authorType(AgentRuleAuthorType.AI)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private ConversationMessage getMessage(final UUID conversationId,
                                           final ConversationParticipantType authorType,
                                           final String authorId,
                                           final String content) {
        return ConversationMessage.builder()
                .id(UUID.randomUUID())
                .conversationId(conversationId)
                .authorType(authorType)
                .authorId(authorId)
                .content(content)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
