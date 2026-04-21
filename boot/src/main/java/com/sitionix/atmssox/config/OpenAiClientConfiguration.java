package com.sitionix.atmssox.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class OpenAiClientConfiguration {

    @Bean
    OpenAIClient openAIClient(final OpenAiChatProperties openAiChatProperties) {
        final OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder()
                .apiKey(openAiChatProperties.getApiKey());

        if (StringUtils.hasText(openAiChatProperties.getBaseUrl())) {
            clientBuilder.baseUrl(openAiChatProperties.getBaseUrl());
        }
        if (StringUtils.hasText(openAiChatProperties.getOrgId())) {
            clientBuilder.organization(openAiChatProperties.getOrgId());
        }
        if (StringUtils.hasText(openAiChatProperties.getProjectId())) {
            clientBuilder.project(openAiChatProperties.getProjectId());
        }

        return clientBuilder.build();
    }
}
