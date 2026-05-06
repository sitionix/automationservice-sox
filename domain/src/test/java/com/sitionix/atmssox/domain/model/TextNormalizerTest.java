package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextNormalizerTest {

    @Test
    void givenRequiredValueWithPadding_whenNormalizeRequired_thenReturnTrimmed() {
        //given
        final String value = "  hello  ";

        //when
        final String actual = TextNormalizer.normalizeRequired(value, "missing", 10, "length");

        //then
        assertThat(actual).isEqualTo("hello");
    }

    @Test
    void givenBlankRequiredValue_whenNormalizeRequired_thenThrowValidation() {
        //given
        final String value = "   ";

        //when/then
        assertThatThrownBy(() -> TextNormalizer.normalizeRequired(value, "missing", 10, "length"))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("missing");
    }

    @Test
    void givenTooLongRequiredValue_whenNormalizeRequired_thenThrowValidation() {
        //given
        final String value = "  hello world  ";

        //when/then
        assertThatThrownBy(() -> TextNormalizer.normalizeRequired(value, "missing", 5, "length"))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("length");
    }

    @Test
    void givenNullOptionalStrictValue_whenNormalizeOptionalStrict_thenReturnNull() {
        //when
        final String actual = TextNormalizer.normalizeOptionalStrict(null, 10, "length", "empty");

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenBlankOptionalStrictValue_whenNormalizeOptionalStrict_thenThrowValidation() {
        //given
        final String value = "  ";

        //when/then
        assertThatThrownBy(() -> TextNormalizer.normalizeOptionalStrict(value, 10, "length", "empty"))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("empty");
    }

    @Test
    void givenTooLongOptionalStrictValue_whenNormalizeOptionalStrict_thenThrowValidation() {
        //given
        final String value = "  hello world  ";

        //when/then
        assertThatThrownBy(() -> TextNormalizer.normalizeOptionalStrict(value, 5, "length", "empty"))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("length");
    }

    @Test
    void givenOptionalNullableValueWithPadding_whenNormalizeOptionalNullable_thenReturnTrimmed() {
        //given
        final String value = "  hello  ";

        //when
        final String actual = TextNormalizer.normalizeOptionalNullable(value, 10, "length");

        //then
        assertThat(actual).isEqualTo("hello");
    }

    @Test
    void givenBlankOptionalNullableValue_whenNormalizeOptionalNullable_thenReturnNull() {
        //given
        final String value = "  ";

        //when
        final String actual = TextNormalizer.normalizeOptionalNullable(value, 10, "length");

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenTooLongOptionalNullableValue_whenNormalizeOptionalNullable_thenThrowValidation() {
        //given
        final String value = "  hello world  ";

        //when/then
        assertThatThrownBy(() -> TextNormalizer.normalizeOptionalNullable(value, 5, "length"))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("length");
    }

    @Test
    void givenNullValue_whenNormalizeToEmpty_thenReturnEmpty() {
        //when
        final String actual = TextNormalizer.normalizeToEmpty(null);

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void givenValueWithPadding_whenNormalizeToEmpty_thenReturnTrimmed() {
        //given
        final String value = "  hello  ";

        //when
        final String actual = TextNormalizer.normalizeToEmpty(value);

        //then
        assertThat(actual).isEqualTo("hello");
    }
}
