package com.sitionix.atmssox.client;

import com.openai.client.OpenAIClient;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceOptions;
import com.sitionix.atmssox.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OpenAiSdkChatClient implements OpenAiChatClient {

    private final OpenAIClient openAIClient;
    private final OpenAiChatProperties openAiChatProperties;
    private final OpenAiNativeToolAdapter nativeToolAdapter;

    @Override
    public String execute(final OpenAiChatRequest request) {
        final OpenAiToolChatResponse response = this.executeWithTools(
                new OpenAiToolChatRequest(request.instruction(), request.input(), null, List.of(), List.of())
        );
        final String output = response.outputText();
        if (!StringUtils.hasText(output)) {
            throw new OpenAiExecutionException("OpenAI returned empty reply");
        }
        return output.trim();
    }

    @Override
    public OpenAiToolChatResponse executeWithTools(final OpenAiToolChatRequest request) {
        this.validateConfiguration();
        if (request == null) {
            throw new OpenAiExecutionException("OpenAI request is not configured");
        }

        try {
            final ResponseCreateParams.Builder builder = ResponseCreateParams.builder()
                    .model(this.openAiChatProperties.getModel())
                    .instructions(request.instruction())
                    .toolChoice(ToolChoiceOptions.AUTO);

            if (StringUtils.hasText(request.previousResponseId())) {
                builder.previousResponseId(request.previousResponseId());
            }

            if (request.toolResults() != null && !request.toolResults().isEmpty()) {
                builder.inputOfResponse(this.nativeToolAdapter.toToolResultInput(request.toolResults()));
            } else {
                builder.input(request.input());
            }

            if (request.tools() != null && !request.tools().isEmpty()) {
                builder.tools(this.nativeToolAdapter.toTools(request.tools()));
            }

            final Response response = this.openAIClient.responses().create(builder.build());
            return new OpenAiToolChatResponse(
                    response.id(),
                    this.nativeToolAdapter.extractOutputText(response),
                    this.nativeToolAdapter.extractToolCalls(response)
            );
        } catch (OpenAiExecutionException exception) {
            throw exception;
        } catch (OpenAIServiceException exception) {
            throw this.mapServiceException(exception);
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

    private OpenAiExecutionException mapServiceException(final OpenAIServiceException exception) {
        return new OpenAiExecutionException(
                exception.statusCode(),
                exception.type().orElse(null),
                exception.code().orElse(null),
                exception.getMessage(),
                exception
        );
    }
}
