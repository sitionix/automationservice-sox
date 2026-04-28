package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
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

    private static final String CAPABILITY_RUNTIME_INSTRUCTION = """
            You can use backend platform capabilities through tools.

            When the user asks about their Sitionix platform data or state, such as sites, workspace, domains, analytics, services, or account information, use DISCOVER_CAPABILITIES before answering.

            Do not say you lack access to platform data before trying available capabilities.
            """.strip();

    private final OpenAiChatClient openAiChatClient;
    private final CapabilityRouterService capabilityRouterService;
    private final DiscoveryCapabilityToolService discoveryCapabilityToolService;
    private final ConcreteCapabilityExecutionService concreteCapabilityExecutionService;
    private final AutomationCapabilitiesProperties capabilitiesProperties;

    public String execute(final String instruction,
                          final String input) {
        final String runtimeInstruction = this.buildRuntimeInstruction(instruction);
        final LoopState state = new LoopState(List.of(this.discoveryCapabilityToolService.getDefinition()));
        log.info(
                "[CAPABILITY] initial tool setup toolsCount={} toolNames={}",
                state.activeTools.size(),
                state.activeTools.stream().map(CapabilityDefinition::name).toList()
        );

        while (true) {
            final OpenAiToolChatResponse response = this.openAiChatClient.executeWithTools(
                    new OpenAiToolChatRequest(runtimeInstruction, input, state.previousResponseId, state.activeTools, state.toolResults)
            );
            state.previousResponseId = response.responseId();
            log.info(
                    "[CAPABILITY] model response received hasToolCalls={} toolCallNames={}",
                    response.toolCalls() != null && !response.toolCalls().isEmpty(),
                    response.toolCalls() == null ? List.of() : response.toolCalls().stream().map(OpenAiNativeToolCall::name).toList()
            );
            if (response.toolCalls() == null || response.toolCalls().isEmpty()) {
                return response.outputText();
            }

            final boolean shouldContinue = this.handleToolCalls(response.toolCalls(), state);
            if (!shouldContinue) {
                return response.outputText();
            }
        }
    }

    private String buildRuntimeInstruction(final String instruction) {
        if (instruction == null || instruction.isBlank()) {
            return CAPABILITY_RUNTIME_INSTRUCTION;
        }
        return instruction.trim() + "\n\n" + CAPABILITY_RUNTIME_INSTRUCTION;
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
                final String userIntent = this.discoveryCapabilityToolService.extractUserIntent(toolCall);
                log.info(
                        "[CAPABILITY] discovery requested userIntentPresent={}",
                        userIntent != null && !userIntent.isBlank()
                );
                final List<CapabilityDefinition> selected = this.capabilityRouterService.discover(userIntent);
                log.info("[CAPABILITY] router selected capabilities={}", selected.stream().map(CapabilityDefinition::name).toList());
                state.activeTools = selected;
                log.info(
                        "[CAPABILITY] concrete tools injected toolsCount={} toolNames={}",
                        state.activeTools.size(),
                        state.activeTools.stream().map(CapabilityDefinition::name).toList()
                );
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
