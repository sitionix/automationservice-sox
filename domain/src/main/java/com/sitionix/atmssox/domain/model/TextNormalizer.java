package com.sitionix.atmssox.domain.model;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextNormalizer {

    public static String normalizeRequired(final String value,
                                           final String missingMessage,
                                           final int maxLength,
                                           final String lengthMessage) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException(missingMessage);
        }
        if (normalized.length() > maxLength) {
            throw new AgentValidationException(lengthMessage);
        }
        return normalized;
    }

    public static String normalizeOptionalStrict(final String value,
                                                 final int maxLength,
                                                 final String lengthMessage,
                                                 final String emptyMessage) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new AgentValidationException(emptyMessage);
        }
        if (normalized.length() > maxLength) {
            throw new AgentValidationException(lengthMessage);
        }
        return normalized;
    }

    public static String normalizeOptionalNullable(final String value,
                                                   final int maxLength,
                                                   final String lengthMessage) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new AgentValidationException(lengthMessage);
        }
        return normalized;
    }

    public static String normalizeToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }
}
