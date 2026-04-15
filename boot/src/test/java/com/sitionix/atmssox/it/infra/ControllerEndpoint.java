package com.sitionix.atmssox.it.infra;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
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

    private ControllerEndpoint() {
    }
}
