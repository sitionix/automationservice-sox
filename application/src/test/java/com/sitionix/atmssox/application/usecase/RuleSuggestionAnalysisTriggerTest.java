package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSuggestionAnalysisTriggerTest {

    private RuleSuggestionAnalysisTrigger ruleSuggestionAnalysisTrigger;

    @Mock
    private RuleSuggestionAnalysisPolicy ruleSuggestionAnalysisPolicy;

    @Mock
    private RuleSuggestionAnalyzerAsyncService ruleSuggestionAnalyzerAsyncService;

    @BeforeEach
    void setUp() {
        this.ruleSuggestionAnalysisTrigger = new RuleSuggestionAnalysisTrigger(
                this.ruleSuggestionAnalysisPolicy,
                this.ruleSuggestionAnalyzerAsyncService
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.ruleSuggestionAnalysisPolicy, this.ruleSuggestionAnalyzerAsyncService);
    }

    @Test
    void givenPolicyAllowsAnalysis_whenSubmitIfAllowed_thenSubmitAnalyzerTask() {
        //given
        final UUID agentId = UUID.fromString("e24e9a2c-290c-4366-9d42-64322f60f12c");
        final UUID conversationId = UUID.fromString("d6a12916-f6d7-4cd4-bda7-e2cbc4f9fd17");
        final ConversationMessage latestUserMessage = this.getLatestUserMessage();
        when(this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage)).thenReturn(true);

        //when
        this.ruleSuggestionAnalysisTrigger.submitIfAllowed(agentId, conversationId, latestUserMessage);

        //then
        verify(this.ruleSuggestionAnalysisPolicy).shouldAnalyze(agentId, conversationId, latestUserMessage);
        verify(this.ruleSuggestionAnalyzerAsyncService).analyzeAsync(agentId, conversationId);
    }

    @Test
    void givenPolicyDeniesAnalysis_whenSubmitIfAllowed_thenDoNotSubmitAnalyzerTask() {
        //given
        final UUID agentId = UUID.fromString("1f7c77fb-dd76-4be6-9703-c0266c594183");
        final UUID conversationId = UUID.fromString("f1af53fe-f76c-4bcc-b0df-4cd1e098da93");
        final ConversationMessage latestUserMessage = this.getLatestUserMessage();
        when(this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage)).thenReturn(false);

        //when
        this.ruleSuggestionAnalysisTrigger.submitIfAllowed(agentId, conversationId, latestUserMessage);

        //then
        verify(this.ruleSuggestionAnalysisPolicy).shouldAnalyze(agentId, conversationId, latestUserMessage);
    }

    @Test
    void givenTaskRejectedException_whenSubmitIfAllowed_thenSwallowException() {
        //given
        final UUID agentId = UUID.fromString("355dd626-9fbf-4f9f-b843-87d43e6375e5");
        final UUID conversationId = UUID.fromString("01f69a39-b803-4f2d-a7a4-9c2e8f2eb9fd");
        final ConversationMessage latestUserMessage = this.getLatestUserMessage();
        when(this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage)).thenReturn(true);
        doThrow(new TaskRejectedException("Task queue full"))
                .when(this.ruleSuggestionAnalyzerAsyncService)
                .analyzeAsync(agentId, conversationId);

        //when
        this.ruleSuggestionAnalysisTrigger.submitIfAllowed(agentId, conversationId, latestUserMessage);

        //then
        verify(this.ruleSuggestionAnalysisPolicy).shouldAnalyze(agentId, conversationId, latestUserMessage);
        verify(this.ruleSuggestionAnalyzerAsyncService).analyzeAsync(agentId, conversationId);
    }

    private ConversationMessage getLatestUserMessage() {
        return ConversationMessage.builder()
                .id(UUID.fromString("8fc662f9-9809-4dbf-bc4a-c8bcc8e2ba3e"))
                .conversationId(UUID.fromString("c2f57dcc-c66d-4f98-8bd8-50577330613c"))
                .authorType(ConversationParticipantType.USER)
                .authorId("17")
                .content("Message")
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
