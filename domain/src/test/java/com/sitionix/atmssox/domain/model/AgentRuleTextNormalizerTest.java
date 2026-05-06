package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentRuleTextNormalizerTest {

    @Test
    void givenTitleWithPadding_whenNormalizeRequiredTitle_thenReturnTrimmed() {
        //when
        final String actual = AgentRuleTextNormalizer.normalizeRequiredTitle("  title  ");

        //then
        assertThat(actual).isEqualTo("title");
    }

    @Test
    void givenBlankTitle_whenNormalizeOptionalTitle_thenThrowValidationException() {
        //when/then
        assertThatThrownBy(() -> AgentRuleTextNormalizer.normalizeOptionalTitle("   "))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Rule title is required");
    }

    @Test
    void givenContentWithPadding_whenNormalizeRequiredContent_thenReturnTrimmed() {
        //when
        final String actual = AgentRuleTextNormalizer.normalizeRequiredContent("  content  ");

        //then
        assertThat(actual).isEqualTo("content");
    }

    @Test
    void givenBlankContent_whenNormalizeOptionalContent_thenThrowValidationException() {
        //when/then
        assertThatThrownBy(() -> AgentRuleTextNormalizer.normalizeOptionalContent("   "))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Rule content is required");
    }

    @Test
    void givenNullValue_whenNormalizeToEmpty_thenReturnEmptyString() {
        //when
        final String actual = AgentRuleTextNormalizer.normalizeToEmpty(null);

        //then
        assertThat(actual).isEqualTo("");
    }
}
