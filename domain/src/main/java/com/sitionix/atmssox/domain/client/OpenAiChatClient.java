package com.sitionix.atmssox.domain.client;

/**
 * Provider boundary for OpenAI chat execution.
 */
public interface OpenAiChatClient {

    /**
     * Executes one chat completion using agent instruction and user message.
     *
     * @param request execution payload prepared by agent execution handler.
     * @return normalized assistant reply text.
     */
    String execute(OpenAiChatRequest request);
}
