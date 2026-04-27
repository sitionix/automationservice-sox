package com.sitionix.atmssox.application.usecase;

import org.springframework.stereotype.Component;

@Component
public class ContextOptimizationCoverageCalculator {

    public int resolveCompressUntilCount(final long totalMessageCount, final int lastMessagesLimit) {
        final long normalizedLimit = Math.max(1, lastMessagesLimit);
        final long candidate = Math.max(0L, totalMessageCount - normalizedLimit);
        return (int) Math.min(Integer.MAX_VALUE, candidate);
    }

    public boolean hasNewCoverage(final int alreadyCoveredCount, final int compressUntilCount) {
        return compressUntilCount > alreadyCoveredCount;
    }
}
