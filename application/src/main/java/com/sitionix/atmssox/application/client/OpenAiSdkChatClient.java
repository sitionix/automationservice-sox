package com.sitionix.atmssox.application.client;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.sitionix.atmssox.application.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OpenAiSdkChatClient implements OpenAiChatClient {

    private final OpenAiChatProperties openAiChatProperties;

    @Override
    public String execute(final String instruction, final String message) {
        this.validateConfiguration();

        try {
            final OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder()
                    .apiKey(this.openAiChatProperties.getApiKey());

            if (StringUtils.hasText(this.openAiChatProperties.getBaseUrl())) {
                clientBuilder.baseUrl(this.openAiChatProperties.getBaseUrl());
            }
            if (StringUtils.hasText(this.openAiChatProperties.getOrgId())) {
                clientBuilder.organization(this.openAiChatProperties.getOrgId());
            }
            if (StringUtils.hasText(this.openAiChatProperties.getProjectId())) {
                clientBuilder.project(this.openAiChatProperties.getProjectId());
            }

            final OpenAIClient openAIClient = clientBuilder.build();

            final ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(this.openAiChatProperties.getModel())
                    .instructions(instruction)
                    .input(message)
                    .build();
            final Response response = openAIClient.responses().create(params);
            final String output = response.output().stream()
                    .flatMap(item -> item.message().stream())
                    .flatMap(outputMessage -> outputMessage.content().stream())
                    .flatMap(content -> content.outputText().stream())
                    .map(outputText -> outputText.text().trim())
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);

            if (!StringUtils.hasText(output)) {
                throw new OpenAiExecutionException("OpenAI returned empty reply");
            }
            return output.trim();
        } catch (OpenAiExecutionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new OpenAiExecutionException("OpenAI request failed", exception);
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(this.openAiChatProperties.getApiKey())) {
            throw new OpenAiExecutionException("OpenAI API key is not configured");
        }
        if (!StringUtils.hasText(this.openAiChatProperties.getModel())) {
            throw new OpenAiExecutionException("OpenAI model is not configured");
        }
    }
}
