package com.sitionix.atmssox.domain.client;

public interface OpenAiToolChatClient {

    OpenAiToolChatResponse executeWithTools(OpenAiToolChatRequest request);
}
