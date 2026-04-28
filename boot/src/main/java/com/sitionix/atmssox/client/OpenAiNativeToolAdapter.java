package com.sitionix.atmssox.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.models.responses.FunctionTool;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.Tool;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OpenAiNativeToolAdapter {

    private final ObjectMapper objectMapper;

    public List<Tool> toTools(final List<CapabilityDefinition> definitions) {
        return definitions.stream().map(this::toTool).toList();
    }

    public List<ResponseInputItem> toToolResultInput(final List<OpenAiNativeToolResult> toolResults) {
        return toolResults.stream().map(result -> ResponseInputItem.ofFunctionCallOutput(
                ResponseInputItem.FunctionCallOutput.builder()
                        .callId(result.callId())
                        .output(ResponseInputItem.FunctionCallOutput.Output.ofString(result.outputJson()))
                        .build()
        )).toList();
    }

    public String extractOutputText(final Response response) {
        return response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(outputMessage -> outputMessage.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text().trim())
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    public List<OpenAiNativeToolCall> extractToolCalls(final Response response) {
        final List<OpenAiNativeToolCall> calls = new ArrayList<>();
        response.output().forEach(item -> item.functionCall().ifPresent(functionCall -> calls.add(
                new OpenAiNativeToolCall(functionCall.callId(), functionCall.name(), functionCall.arguments())
        )));
        return calls;
    }

    private Map<String, JsonValue> toJsonMap(final Object inputSchema) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = inputSchema == null ? Map.of() : this.objectMapper.convertValue(inputSchema, Map.class);
        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> JsonValue.from(entry.getValue())
        ));
    }

    private Tool toTool(final CapabilityDefinition definition) {
        return Tool.ofFunction(
                FunctionTool.builder()
                        .name(definition.name())
                        .description(definition.description())
                        .strict(true)
                        .parameters(FunctionTool.Parameters.builder()
                                .additionalProperties(this.toJsonMap(definition.inputSchema()))
                                .build())
                        .build()
        );
    }
}
