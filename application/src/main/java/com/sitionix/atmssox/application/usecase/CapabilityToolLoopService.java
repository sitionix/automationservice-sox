package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapabilityToolLoopService {

    private final OpenAiChatClient openAiChatClient;
    private final CapabilityRouterService capabilityRouterService;
    private final DiscoveryCapabilityToolService discoveryCapabilityToolService;
    private final ConcreteCapabilityExecutionService concreteCapabilityExecutionService;
    private final AutomationCapabilitiesProperties capabilitiesProperties;

    public String execute(final String instruction, final String input) {
        final LoopState state = new LoopState(List.of(this.discoveryCapabilityToolService.getDefinition()));

        while (true) {
            final OpenAiToolChatResponse response = this.openAiChatClient.executeWithTools(
                    new OpenAiToolChatRequest(instruction, input, state.previousResponseId, state.activeTools, state.toolResults)
            );
            state.previousResponseId = response.responseId();
            if (response.toolCalls() == null || response.toolCalls().isEmpty()) {
                return response.outputText();
            }

            final boolean shouldContinue = this.handleToolCalls(response.toolCalls(), state);
            if (!shouldContinue) {
                return response.outputText();
            }
        }
    }

    private boolean handleToolCalls(final List<OpenAiNativeToolCall> toolCalls,
                                    final LoopState state) {
        final List<OpenAiNativeToolResult> stepResults = new ArrayList<>();
        for (final OpenAiNativeToolCall toolCall : toolCalls) {
            if (this.discoveryCapabilityToolService.isDiscoveryCall(toolCall)) {
                if (state.discoveryCalls >= this.capabilitiesProperties.getExecution().getMaxDiscoveryCallsPerMessage()) {
                    log.info("[CAPABILITY] execution limits reached discoveryCalls={}", state.discoveryCalls);
                    return false;
                }
                state.discoveryCalls++;
                log.info("[CAPABILITY] discovery requested");
                final String userIntent = this.discoveryCapabilityToolService.extractUserIntent(toolCall);
                final List<CapabilityDefinition> selected = this.capabilityRouterService.discover(userIntent);
                log.info("[CAPABILITY] router selected capabilities={}", selected.stream().map(CapabilityDefinition::name).toList());
                state.activeTools = selected;
                stepResults.add(this.discoveryCapabilityToolService.buildDiscoveryResult(toolCall, selected));
                continue;
            }

            if (state.capabilityCalls >= this.capabilitiesProperties.getExecution().getMaxCapabilityCallsPerMessage()) {
                log.info("[CAPABILITY] execution limits reached capabilityCalls={}", state.capabilityCalls);
                return false;
            }
            state.capabilityCalls++;
            stepResults.add(this.concreteCapabilityExecutionService.execute(toolCall));
        }
        state.toolResults = stepResults;
        return true;
    }

    private static final class LoopState {
        private int discoveryCalls;
        private int capabilityCalls;
        private String previousResponseId;
        private List<CapabilityDefinition> activeTools;
        private List<OpenAiNativeToolResult> toolResults;

        private LoopState(final List<CapabilityDefinition> initialTools) {
            this.discoveryCalls = 0;
            this.capabilityCalls = 0;
            this.previousResponseId = null;
            this.activeTools = initialTools;
            this.toolResults = List.of();
        }
    }
}
