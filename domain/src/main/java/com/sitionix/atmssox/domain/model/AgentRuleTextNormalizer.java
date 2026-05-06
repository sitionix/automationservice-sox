package com.sitionix.atmssox.domain.model;

public final class AgentRuleTextNormalizer {

    private AgentRuleTextNormalizer() {
    }

    public static String normalizeRequiredTitle(final String value) {
        return TextNormalizer.normalizeRequired(
                value,
                "Rule title is required",
                Integer.MAX_VALUE,
                "Rule title is required"
        );
    }

    public static String normalizeRequiredContent(final String value) {
        return TextNormalizer.normalizeRequired(
                value,
                "Rule content is required",
                Integer.MAX_VALUE,
                "Rule content is required"
        );
    }

    public static String normalizeOptionalTitle(final String value) {
        return TextNormalizer.normalizeOptionalStrict(
                value,
                Integer.MAX_VALUE,
                "Rule title is required",
                "Rule title is required"
        );
    }

    public static String normalizeOptionalContent(final String value) {
        return TextNormalizer.normalizeOptionalStrict(
                value,
                Integer.MAX_VALUE,
                "Rule content is required",
                "Rule content is required"
        );
    }

    public static String normalizeToEmpty(final String value) {
        return TextNormalizer.normalizeToEmpty(value);
    }
}
