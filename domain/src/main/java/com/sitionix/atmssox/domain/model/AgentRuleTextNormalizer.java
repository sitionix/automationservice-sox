package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentValidationException;

public final class AgentRuleTextNormalizer {

    private AgentRuleTextNormalizer() {
    }

    public static String normalizeRequired(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Rule text is required");
        }
        return normalized;
    }
}
