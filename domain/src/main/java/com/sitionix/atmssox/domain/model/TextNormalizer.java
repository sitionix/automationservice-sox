package com.sitionix.atmssox.domain.model;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }
}
