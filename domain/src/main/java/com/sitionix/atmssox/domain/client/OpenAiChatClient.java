package com.sitionix.atmssox.domain.client;

public interface OpenAiChatClient {

    String execute(OpenAiChatRequest request);

    OpenAiToolChatResponse executeWithTools(OpenAiToolChatRequest request);
}
