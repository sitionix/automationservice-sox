package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSuggestionAnalysisPolicyTest {

    private RuleSuggestionAnalysisPolicy ruleSuggestionAnalysisPolicy;

    @Mock
    private RuleSuggestionAnalyzerProperties properties;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @BeforeEach
    void setUp() {
        this.ruleSuggestionAnalysisPolicy = new RuleSuggestionAnalysisPolicy(
                this.properties,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationMessageRepository
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.properties,
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationMessageRepository
        );
    }

    @Test
    void givenPolicyDisabled_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("80e2bb3e-dd5f-4fdb-8147-cda83f849558");
        final UUID conversationId = UUID.fromString("64839ff7-b872-4a4b-bdd7-995585de07c1");
        final ConversationMessage latestUserMessage = this.getMessage(ConversationParticipantType.USER);
        when(this.properties.isEnabled()).thenReturn(false);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(agentId, conversationId, latestUserMessage);

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verifyNoInteractions(this.agentRepository, this.agentRuleRepository, this.conversationMessageRepository);
    }

    @Test
    void givenLatestMessageNotUser_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("e4ed44b3-f36d-4b80-95db-f9f0c27f6cd8");
        final UUID conversationId = UUID.fromString("80232449-f69b-42f2-9581-baf2384d765c");
        when(this.properties.isEnabled()).thenReturn(true);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.AGENT)
        );

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verifyNoInteractions(this.agentRepository, this.agentRuleRepository, this.conversationMessageRepository);
    }

    @Test
    void givenMissingTargetAgent_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("0bfbc45e-2e9d-4be3-9dc6-25f653641a9b");
        final UUID conversationId = UUID.fromString("28fef9b8-e501-4149-bf6f-fd76657fca0b");
        when(this.properties.isEnabled()).thenReturn(true);
        when(this.agentRepository.findById(agentId)).thenReturn(Optional.empty());

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verify(this.agentRepository).findById(agentId);
        verifyNoInteractions(this.agentRuleRepository, this.conversationMessageRepository);
    }

    @Test
    void givenPendingSuggestionsReachedLimit_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("8d0055e5-7da3-4297-a9fd-ec49167eab6c");
        final UUID conversationId = UUID.fromString("f897b306-7506-4c89-9cad-c895224f23aa");
        when(this.properties.isEnabled()).thenReturn(true);
        when(this.agentRepository.findById(agentId)).thenReturn(Optional.of(this.getActiveUserAgent(agentId)));
        when(this.properties.getMaxPendingSuggestionsPerAgent()).thenReturn(3);
        when(this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI))
                .thenReturn(3L);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verify(this.agentRepository).findById(agentId);
        verify(this.properties).getMaxPendingSuggestionsPerAgent();
        verify(this.agentRuleRepository).countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        verifyNoInteractions(this.conversationMessageRepository);
    }

    @Test
    void givenCooldownNotPassed_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("685ec86f-42e7-4987-9240-dd2b16243b8e");
        final UUID conversationId = UUID.fromString("9004fefb-8ac2-41ba-b97b-53eb6ca63884");
        when(this.properties.isEnabled()).thenReturn(true);
        when(this.agentRepository.findById(agentId)).thenReturn(Optional.of(this.getActiveUserAgent(agentId)));
        when(this.properties.getMaxPendingSuggestionsPerAgent()).thenReturn(5);
        when(this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI))
                .thenReturn(1L);
        when(this.agentRuleRepository.findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI))
                .thenReturn(Optional.of(Instant.now().minusSeconds(300)));
        when(this.properties.getConversationCooldownMinutes()).thenReturn(30);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verify(this.agentRepository).findById(agentId);
        verify(this.properties).getMaxPendingSuggestionsPerAgent();
        verify(this.agentRuleRepository).countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        verify(this.agentRuleRepository).findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI);
        verify(this.properties).getConversationCooldownMinutes();
        verifyNoInteractions(this.conversationMessageRepository);
    }

    @Test
    void givenDailyQuotaExceeded_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("671e7f35-bde2-4e84-a7f2-45ac9de8f0ac");
        final UUID conversationId = UUID.fromString("5f7dc46e-3f1d-44da-a587-ca4aa09caa0b");
        when(this.properties.isEnabled()).thenReturn(true);
        when(this.agentRepository.findById(agentId)).thenReturn(Optional.of(this.getActiveUserAgent(agentId)));
        when(this.properties.getMaxPendingSuggestionsPerAgent()).thenReturn(5);
        when(this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI))
                .thenReturn(1L);
        when(this.agentRuleRepository.findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI))
                .thenReturn(Optional.of(Instant.now().minusSeconds(3700)));
        when(this.properties.getConversationCooldownMinutes()).thenReturn(30);
        when(this.agentRuleRepository.countByAgentIdAndAuthorTypeAndCreatedAtBetween(
                eq(agentId),
                eq(AgentRuleAuthorType.AI),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(3L);
        when(this.properties.getMaxAgentAnalysesPerDay()).thenReturn(3);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isFalse();
        verify(this.properties).isEnabled();
        verify(this.agentRepository).findById(agentId);
        verify(this.properties).getMaxPendingSuggestionsPerAgent();
        verify(this.agentRuleRepository).countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        verify(this.agentRuleRepository).findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI);
        verify(this.properties).getConversationCooldownMinutes();
        verify(this.agentRuleRepository).countByAgentIdAndAuthorTypeAndCreatedAtBetween(
                eq(agentId),
                eq(AgentRuleAuthorType.AI),
                any(Instant.class),
                any(Instant.class)
        );
        verify(this.properties).getMaxAgentAnalysesPerDay();
        verifyNoInteractions(this.conversationMessageRepository);
    }

    @Test
    void givenMessageCountBelowThreshold_whenShouldAnalyze_thenReturnFalse() {
        //given
        final UUID agentId = UUID.fromString("a089f9be-ae9f-4ce4-b250-2825ca7d2b73");
        final UUID conversationId = UUID.fromString("a1f9f00b-c51d-4694-bb4f-fca0c4bcf5db");
        this.stubPositivePreconditions(agentId);
        when(this.conversationMessageRepository.countByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(2L);
        when(this.properties.getMessageCountThreshold()).thenReturn(3);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isFalse();
        this.verifyPositivePreconditions(agentId);
        verify(this.conversationMessageRepository).countByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verify(this.properties).getMessageCountThreshold();
    }

    @Test
    void givenAllConditionsSatisfied_whenShouldAnalyze_thenReturnTrue() {
        //given
        final UUID agentId = UUID.fromString("3f16a654-017d-4f8d-b2d0-a4d2ecad0d5a");
        final UUID conversationId = UUID.fromString("f6a07326-00bc-4bd7-8fc0-f8cc8e3eb8e4");
        this.stubPositivePreconditions(agentId);
        when(this.conversationMessageRepository.countByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER))
                .thenReturn(5L);
        when(this.properties.getMessageCountThreshold()).thenReturn(3);

        //when
        final boolean actual = this.ruleSuggestionAnalysisPolicy.shouldAnalyze(
                agentId,
                conversationId,
                this.getMessage(ConversationParticipantType.USER)
        );

        //then
        assertThat(actual).isTrue();
        this.verifyPositivePreconditions(agentId);
        verify(this.conversationMessageRepository).countByConversationIdAndAuthorType(conversationId, ConversationParticipantType.USER);
        verify(this.properties).getMessageCountThreshold();
    }

    private void stubPositivePreconditions(final UUID agentId) {
        when(this.properties.isEnabled()).thenReturn(true);
        when(this.agentRepository.findById(agentId)).thenReturn(Optional.of(this.getActiveUserAgent(agentId)));
        when(this.properties.getMaxPendingSuggestionsPerAgent()).thenReturn(5);
        when(this.agentRuleRepository.countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI))
                .thenReturn(1L);
        when(this.agentRuleRepository.findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI))
                .thenReturn(Optional.of(Instant.now().minusSeconds(3700)));
        when(this.properties.getConversationCooldownMinutes()).thenReturn(30);
        when(this.agentRuleRepository.countByAgentIdAndAuthorTypeAndCreatedAtBetween(
                eq(agentId),
                eq(AgentRuleAuthorType.AI),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(0L);
        when(this.properties.getMaxAgentAnalysesPerDay()).thenReturn(3);
    }

    private void verifyPositivePreconditions(final UUID agentId) {
        verify(this.properties).isEnabled();
        verify(this.agentRepository).findById(agentId);
        verify(this.properties).getMaxPendingSuggestionsPerAgent();
        verify(this.agentRuleRepository).countByAgentIdAndStatusAndAuthorType(agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        verify(this.agentRuleRepository).findLastCreatedAtByAgentIdAndAuthorType(agentId, AgentRuleAuthorType.AI);
        verify(this.properties).getConversationCooldownMinutes();
        verify(this.agentRuleRepository).countByAgentIdAndAuthorTypeAndCreatedAtBetween(
                eq(agentId),
                eq(AgentRuleAuthorType.AI),
                any(Instant.class),
                any(Instant.class)
        );
        verify(this.properties).getMaxAgentAnalysesPerDay();
    }

    private Agent getActiveUserAgent(final UUID agentId) {
        return Agent.builder()
                .id(agentId)
                .userId(17L)
                .name("Name")
                .description("Description")
                .instruction("Instruction")
                .type(AgentType.USER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private ConversationMessage getMessage(final ConversationParticipantType authorType) {
        return ConversationMessage.builder()
                .id(UUID.fromString("dcdfa848-4aa5-4d09-b453-f738ba1ecb8c"))
                .conversationId(UUID.fromString("dd1e03df-8ea7-49d2-9913-c34c2b2ebfff"))
                .authorType(authorType)
                .authorId(authorType == ConversationParticipantType.USER ? "17" : "agent")
                .content("Message")
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
