package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentValidationException;

public final class AgentRuleTextNormalizer {

    private AgentRuleTextNormalizer() {
    }

    public static String normalizeRequiredTitle(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Rule title is required");
        }
        return normalized;
    }

    public static String normalizeRequiredContent(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Rule content is required");
        }
        return normalized;
    }

    public static String normalizeOptionalTitle(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new AgentValidationException("Rule title is required");
        }
        return normalized;
    }

    public static String normalizeOptionalContent(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new AgentValidationException("Rule content is required");
        }
        return normalized;
    }

    public static String normalizeToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }
}
