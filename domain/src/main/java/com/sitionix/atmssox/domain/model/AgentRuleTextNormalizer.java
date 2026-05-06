package com.sitionix.atmssox.domain.model;

public final class AgentRuleTextNormalizer {

    private static final String RULE_TITLE_REQUIRED_MESSAGE = "Rule title is required";
    private static final String RULE_CONTENT_REQUIRED_MESSAGE = "Rule content is required";

    private AgentRuleTextNormalizer() {
    }

    public static String normalizeRequiredTitle(final String value) {
        return TextNormalizer.normalizeRequired(
                value,
                RULE_TITLE_REQUIRED_MESSAGE,
                Integer.MAX_VALUE,
                RULE_TITLE_REQUIRED_MESSAGE
        );
    }

    public static String normalizeRequiredContent(final String value) {
        return TextNormalizer.normalizeRequired(
                value,
                RULE_CONTENT_REQUIRED_MESSAGE,
                Integer.MAX_VALUE,
                RULE_CONTENT_REQUIRED_MESSAGE
        );
    }

    public static String normalizeOptionalTitle(final String value) {
        return TextNormalizer.normalizeOptionalStrict(
                value,
                Integer.MAX_VALUE,
                RULE_TITLE_REQUIRED_MESSAGE,
                RULE_TITLE_REQUIRED_MESSAGE
        );
    }

    public static String normalizeOptionalContent(final String value) {
        return TextNormalizer.normalizeOptionalStrict(
                value,
                Integer.MAX_VALUE,
                RULE_CONTENT_REQUIRED_MESSAGE,
                RULE_CONTENT_REQUIRED_MESSAGE
        );
    }

    public static String normalizeToEmpty(final String value) {
        return TextNormalizer.normalizeToEmpty(value);
    }
}
