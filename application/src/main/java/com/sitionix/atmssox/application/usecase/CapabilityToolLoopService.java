package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiToolChatRequest;
import com.sitionix.atmssox.domain.client.OpenAiToolChatResponse;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapabilityToolLoopService {

    private static final String CAPABILITY_RUNTIME_INSTRUCTION = """
            You have access to backend platform capabilities through tools.

            If the user asks about their platform data, workspace, sites, domains, analytics, services, account state, or any information that may require backend data, you must call DISCOVER_CAPABILITIES before answering.

            Do not claim that you lack access to platform data before trying DISCOVER_CAPABILITIES.

            If DISCOVER_CAPABILITIES returns no relevant capabilities, then answer normally and explain what information is missing if needed.
            """.strip();

    private final OpenAiChatClient openAiChatClient;
    private final CapabilityRouterService capabilityRouterService;
    private final DiscoveryCapabilityToolService discoveryCapabilityToolService;
    private final ConcreteCapabilityExecutionService concreteCapabilityExecutionService;
    private final AutomationCapabilitiesProperties capabilitiesProperties;

    public String execute(final UUID agentId,
                          final UUID conversationId,
                          final String instruction,
                          final String input) {
        final String runtimeInstruction = this.buildRuntimeInstruction(instruction);
        final LoopState state = new LoopState(List.of(this.discoveryCapabilityToolService.getDefinition()));
        final String normalizedInstruction = runtimeInstruction.toLowerCase();
        final boolean capabilityInstructionPresent = normalizedInstruction.contains("capab")
                || normalizedInstruction.contains("tool")
                || normalizedInstruction.contains("backend")
                || normalizedInstruction.contains("platform")
                || normalizedInstruction.contains("workspace")
                || normalizedInstruction.contains("domain")
                || normalizedInstruction.contains("site");
        log.info(
                "[CAPABILITY] initial tool setup agentId={} conversationId={} toolsCount={} toolNames={}",
                agentId,
                conversationId,
                state.activeTools.size(),
                state.activeTools.stream().map(CapabilityDefinition::name).toList()
        );
        log.info("[CAPABILITY_DIAG] capability instruction present={}", capabilityInstructionPresent);

        while (true) {
            final OpenAiToolChatResponse response = this.openAiChatClient.executeWithTools(
                    new OpenAiToolChatRequest(runtimeInstruction, input, state.previousResponseId, state.activeTools, state.toolResults)
            );
            state.previousResponseId = response.responseId();
            log.info(
                    "[CAPABILITY] model response received agentId={} conversationId={} hasToolCalls={} toolCallNames={}",
                    agentId,
                    conversationId,
                    response.toolCalls() != null && !response.toolCalls().isEmpty(),
                    response.toolCalls() == null ? List.of() : response.toolCalls().stream().map(OpenAiNativeToolCall::name).toList()
            );
            if (response.toolCalls() == null || response.toolCalls().isEmpty()) {
                return response.outputText();
            }

            final boolean shouldContinue = this.handleToolCalls(agentId, conversationId, response.toolCalls(), state);
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

    private boolean handleToolCalls(final UUID agentId,
                                    final UUID conversationId,
                                    final List<OpenAiNativeToolCall> toolCalls,
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
                        "[CAPABILITY] discovery requested agentId={} conversationId={} userIntentPresent={}",
                        agentId,
                        conversationId,
                        userIntent != null && !userIntent.isBlank()
                );
                final List<CapabilityDefinition> selected = this.capabilityRouterService.discover(agentId, conversationId, userIntent);
                log.info("[CAPABILITY] router selected capabilities={}", selected.stream().map(CapabilityDefinition::name).toList());
                state.activeTools = selected;
                log.info(
                        "[CAPABILITY] concrete tools injected agentId={} conversationId={} toolsCount={} toolNames={}",
                        agentId,
                        conversationId,
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
            stepResults.add(this.concreteCapabilityExecutionService.execute(agentId, conversationId, toolCall));
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
