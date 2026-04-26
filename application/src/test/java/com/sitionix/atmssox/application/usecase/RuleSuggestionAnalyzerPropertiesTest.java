package com.sitionix.atmssox.application.usecase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleSuggestionAnalyzerPropertiesTest {

    @Test
    void givenNewProperties_whenReadDefaults_thenReturnConfiguredDefaults() {
        //given
        final RuleSuggestionAnalyzerProperties actual = new RuleSuggestionAnalyzerProperties();

        //when

        //then
        assertThat(actual.isEnabled()).isTrue();
        assertThat(actual.getLastMessagesLimit()).isEqualTo(20);
        assertThat(actual.getMessageCountThreshold()).isEqualTo(10);
        assertThat(actual.getConversationCooldownMinutes()).isEqualTo(30);
        assertThat(actual.getMaxAgentAnalysesPerDay()).isEqualTo(3);
        assertThat(actual.getMaxPendingSuggestionsPerAgent()).isEqualTo(5);
        assertThat(actual.getMaxSuggestionsPerRun()).isEqualTo(3);
        assertThat(actual.getMaxSuggestionContentLength()).isEqualTo(1000);
    }

    @Test
    void givenValues_whenSettersCalled_thenReturnUpdatedValues() {
        //given
        final RuleSuggestionAnalyzerProperties actual = new RuleSuggestionAnalyzerProperties();

        //when
        actual.setEnabled(false);
        actual.setLastMessagesLimit(9);
        actual.setMessageCountThreshold(4);
        actual.setConversationCooldownMinutes(12);
        actual.setMaxAgentAnalysesPerDay(8);
        actual.setMaxPendingSuggestionsPerAgent(7);
        actual.setMaxSuggestionsPerRun(6);
        actual.setMaxSuggestionContentLength(500);

        //then
        assertThat(actual.isEnabled()).isFalse();
        assertThat(actual.getLastMessagesLimit()).isEqualTo(9);
        assertThat(actual.getMessageCountThreshold()).isEqualTo(4);
        assertThat(actual.getConversationCooldownMinutes()).isEqualTo(12);
        assertThat(actual.getMaxAgentAnalysesPerDay()).isEqualTo(8);
        assertThat(actual.getMaxPendingSuggestionsPerAgent()).isEqualTo(7);
        assertThat(actual.getMaxSuggestionsPerRun()).isEqualTo(6);
        assertThat(actual.getMaxSuggestionContentLength()).isEqualTo(500);
    }
}
