package com.sitionix.atmssox.it.infra;

import com.app_afesox.atmssox.api_first.dto.Agent;
import com.app_afesox.atmssox.api_first.dto.AgentsResponse;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequest;
import com.sitionix.forgeit.domain.endpoint.Endpoint;
import com.sitionix.forgeit.domain.endpoint.HttpMethod;
import com.sitionix.forgeit.domain.endpoint.mockmvc.MockmvcDefault;

public class ControllerEndpoint {

    public static Endpoint<CreateAgentRequest, Agent> createAgent() {
        return Endpoint.createContract(
                "/api/v1/agents",
                HttpMethod.POST,
                CreateAgentRequest.class,
                Agent.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("createAgentRequest.json")
                        .expectStatus(201)
        );
    }

    public static Endpoint<Void, AgentsResponse> getAgents() {
        return Endpoint.createContract(
                "/api/v1/agents",
                HttpMethod.GET,
                Void.class,
                AgentsResponse.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, Agent> getAgent() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}",
                HttpMethod.GET,
                Void.class,
                Agent.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    private ControllerEndpoint() {
    }
}
