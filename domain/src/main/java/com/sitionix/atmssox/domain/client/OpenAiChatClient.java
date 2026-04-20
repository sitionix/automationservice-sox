package com.sitionix.atmssox.domain.client;

/**
 * Provider boundary for OpenAI chat execution.
 */
public interface OpenAiChatClient {

    /**
     * Executes one chat completion using agent instruction and user message.
     *
     * @param instruction system behavior instruction from the agent definition.
     * @param message user message to process.
     * @return normalized assistant reply text.
     */
    String execute(String instruction, String message);
}
