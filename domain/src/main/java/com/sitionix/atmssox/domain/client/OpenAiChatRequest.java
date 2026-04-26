package com.sitionix.atmssox.domain.client;

/**
 * Provider-agnostic chat execution payload.
 *
 * @param instruction system behavior instruction from the agent definition
 * @param input user/content input prepared by execution handler
 */
public record OpenAiChatRequest(
        String instruction,
        String input
) {
}
