package com.sitionix.atmssox.client;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.sitionix.atmssox.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OpenAiSdkChatClient implements OpenAiChatClient {

    private final OpenAIClient openAIClient;

    private final OpenAiChatProperties openAiChatProperties;

    @Override
    public String execute(final String instruction, final String message) {
        this.validateConfiguration();

        try {
            final ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(this.openAiChatProperties.getModel())
                    .instructions(instruction)
                    .input(message)
                    .build();
            final Response response = this.openAIClient.responses().create(params);
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
        final String upstreamMessage = this.resolveUpstreamMessage(exception);
        final String upstreamType = this.resolveUpstreamType(exception);
        final String upstreamCode = this.resolveUpstreamCode(exception);
        return new OpenAiExecutionException(
                exception.statusCode(),
                upstreamType,
                upstreamCode,
                upstreamMessage,
                exception
        );
    }

    private String resolveUpstreamMessage(final OpenAIServiceException exception) {
        final String parsedMessage = this.resolveBodyField(exception.body(), "message");
        if (StringUtils.hasText(parsedMessage)) {
            return parsedMessage;
        }
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }
        return "OpenAI request failed";
    }

    private String resolveUpstreamType(final OpenAIServiceException exception) {
        if (exception.type().isPresent() && StringUtils.hasText(exception.type().get())) {
            return exception.type().get();
        }
        return this.resolveBodyField(exception.body(), "type");
    }

    private String resolveUpstreamCode(final OpenAIServiceException exception) {
        if (exception.code().isPresent() && StringUtils.hasText(exception.code().get())) {
            return exception.code().get();
        }
        return this.resolveBodyField(exception.body(), "code");
    }

    private String resolveBodyField(final JsonValue body, final String fieldName) {
        try {
            final Map<String, Object> bodyMap = body.convert(Map.class);
            final Object errorNode = bodyMap.get("error");
            if (errorNode instanceof Map<?, ?> errorMap) {
                final Object nestedFieldValue = errorMap.get(fieldName);
                if (nestedFieldValue instanceof String nestedFieldAsString
                        && StringUtils.hasText(nestedFieldAsString)) {
                    return nestedFieldAsString;
                }
            }
            final Object fieldValue = bodyMap.get(fieldName);
            if (fieldValue instanceof String fieldAsString && StringUtils.hasText(fieldAsString)) {
                return fieldAsString;
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
