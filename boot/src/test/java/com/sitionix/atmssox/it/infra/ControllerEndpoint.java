package com.sitionix.atmssox.it.infra;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.forgeit.domain.endpoint.Endpoint;
import com.sitionix.forgeit.domain.endpoint.HttpMethod;
import com.sitionix.forgeit.domain.endpoint.mockmvc.MockmvcDefault;

public class ControllerEndpoint {

    public static Endpoint<CreateAgentRequestDTO, AgentDTO> createAgent() {
        return Endpoint.createContract(
                "/api/v1/agents",
                HttpMethod.POST,
                CreateAgentRequestDTO.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("createAgentRequest.json")
                        .expectStatus(201)
        );
    }

    public static Endpoint<Void, AgentsResponseDTO> getAgents() {
        return Endpoint.createContract(
                "/api/v1/agents",
                HttpMethod.GET,
                Void.class,
                AgentsResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentDTO> getAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}",
                HttpMethod.GET,
                Void.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<PatchAgentRequestDTO, AgentDTO> patchAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}",
                HttpMethod.PATCH,
                PatchAgentRequestDTO.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("patchAgentRequest.json")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentRulesResponseDTO> getAgentRules() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules",
                HttpMethod.GET,
                Void.class,
                AgentRulesResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<CreateAgentRuleRequestDTO, AgentRuleDTO> createAgentRule() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules",
                HttpMethod.POST,
                CreateAgentRuleRequestDTO.class,
                AgentRuleDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("createAgentRuleRequest.json")
                        .expectStatus(201)
        );
    }

    public static Endpoint<PatchAgentRuleRequestDTO, AgentRuleDTO> patchAgentRule() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules/{ruleId}",
                HttpMethod.PATCH,
                PatchAgentRuleRequestDTO.class,
                AgentRuleDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("patchAgentRuleRequest.json")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, DeleteAgentRuleResponseDTO> deleteAgentRule() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules/{ruleId}",
                HttpMethod.DELETE,
                Void.class,
                DeleteAgentRuleResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<AcceptAgentRuleRequestDTO, AgentRuleDTO> acceptAgentRule() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules/{ruleId}/accept",
                HttpMethod.POST,
                AcceptAgentRuleRequestDTO.class,
                AgentRuleDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("acceptAgentRuleRequest.json")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentRuleDTO> rejectAgentRule() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/rules/{ruleId}/reject",
                HttpMethod.POST,
                Void.class,
                AgentRuleDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentDTO> activateAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/activate",
                HttpMethod.POST,
                Void.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentDTO> archiveAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/archive",
                HttpMethod.POST,
                Void.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<ChatAgentRequestDTO, SubmitChatExecutionResponseDTO> chatAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/chat",
                HttpMethod.POST,
                ChatAgentRequestDTO.class,
                SubmitChatExecutionResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("chatAgentRequest.json")
                        .expectStatus(202)
        );
    }

    public static Endpoint<Void, AgentDTO> restoreAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/restore",
                HttpMethod.POST,
                Void.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentDTO> deleteAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}",
                HttpMethod.DELETE,
                Void.class,
                AgentDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    private ControllerEndpoint() {
    }
}
