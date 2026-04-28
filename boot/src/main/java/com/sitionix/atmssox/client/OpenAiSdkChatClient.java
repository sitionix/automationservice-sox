package com.sitionix.atmssox.client;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.Tool;
import com.openai.models.responses.ToolChoiceOptions;
import com.sitionix.atmssox.config.OpenAiChatProperties;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolDefinition;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import java.util.ArrayList;
import java.util.List;
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
                builder.inputOfResponse(this.toToolResultInput(request.toolResults()));
            } else {
                builder.input(request.input());
            }

            if (request.tools() != null && !request.tools().isEmpty()) {
                builder.tools(this.toTools(request.tools()));
            }

            final Response response = this.openAIClient.responses().create(builder.build());
            return new OpenAiToolChatResponse(
                    response.id(),
                    this.extractOutputText(response),
                    this.extractToolCalls(response)
            );
        } catch (OpenAiExecutionException exception) {
            throw exception;
        } catch (OpenAIServiceException exception) {
            throw this.mapServiceException(exception);
        } catch (Exception exception) {
            throw new OpenAiExecutionException("OpenAI request failed", exception);
        }
    }

    private List<Tool> toTools(final List<OpenAiNativeToolDefinition> definitions) {
        return definitions.stream().map(definition -> Tool.ofFunction(
                FunctionTool.builder()
                        .name(definition.name())
                        .description(definition.description())
                        .strict(definition.strict())
                        .parameters(FunctionTool.Parameters.builder()
                                .additionalProperties(this.toJsonMap(definition.inputSchema()))
                                .build())
                        .build()
        )).toList();
    }

    private List<ResponseInputItem> toToolResultInput(final List<OpenAiNativeToolResult> toolResults) {
        return toolResults.stream().map(result -> ResponseInputItem.ofFunctionCallOutput(
                ResponseInputItem.FunctionCallOutput.builder()
                        .callId(result.callId())
                        .output(ResponseInputItem.FunctionCallOutput.Output.ofString(result.outputJson()))
                        .build()
        )).toList();
    }

    private String extractOutputText(final Response response) {
        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(outputMessage -> outputMessage.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text().trim())
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private List<OpenAiNativeToolCall> extractToolCalls(final Response response) {
        final List<OpenAiNativeToolCall> calls = new ArrayList<>();
        response.output().forEach(item -> item.functionCall().ifPresent(functionCall -> calls.add(
                new OpenAiNativeToolCall(functionCall.callId(), functionCall.name(), functionCall.arguments())
        )));
        return calls;
    }

    private Map<String, JsonValue> toJsonMap(final com.fasterxml.jackson.databind.JsonNode jsonNode) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = jsonNode == null ? Map.of() : com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .convertValue(jsonNode, Map.class);
        return map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> JsonValue.from(entry.getValue())
        ));
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
