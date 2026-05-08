package com.sitionix.atmssox.it.infra;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AcceptAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.AddAgentToProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectDTO;
import com.app_afesox.atmssox.api_first.dto.AgentProjectsPageResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectAgentsResponseDTO;
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

    public static Endpoint<ChatAgentRequestDTO, SubmitChatExecutionResponseDTO> chatAgentByExecutionsPath() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/chat/executions",
                HttpMethod.POST,
                ChatAgentRequestDTO.class,
                SubmitChatExecutionResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("chatAgentRequest.json")
                        .expectStatus(202)
        );
    }

    public static Endpoint<Void, ChatExecutionDTO> getAgentChatExecution() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/chat/executions/{executionId}",
                HttpMethod.GET,
                Void.class,
                ChatExecutionDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentConversationsResponseDTO> getAgentConversations() {
        return Endpoint.createContract(
                "/api/v1/agents/{agentId}/conversations",
                HttpMethod.GET,
                Void.class,
                AgentConversationsResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentConversationDetailsDTO> getAgentConversation() {
        return Endpoint.createContract(
                "/api/v1/conversations/{conversationId}",
                HttpMethod.GET,
                Void.class,
                AgentConversationDetailsDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
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

    public static Endpoint<Void, Void> deleteAgentConversation() {
        return Endpoint.createContract(
                "/api/v1/conversations/{conversationId}",
                HttpMethod.DELETE,
                Void.class,
                Void.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(204)
        );
    }

    public static Endpoint<CreateAgentProjectRequestDTO, AgentProjectDTO> createAgentProject() {
        return createAgentProject("1");
    }

    public static Endpoint<CreateAgentProjectRequestDTO, AgentProjectDTO> createAgentProject(final String userSub) {
        return Endpoint.createContract(
                "/api/v1/agent-projects",
                HttpMethod.POST,
                CreateAgentProjectRequestDTO.class,
                AgentProjectDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", userSub)
                        .withRequest("createAgentProjectRequest.json")
                        .expectStatus(201)
        );
    }

    public static Endpoint<Void, AgentProjectsPageResponseDTO> getAgentProjects() {
        return getAgentProjects("1");
    }

    public static Endpoint<Void, AgentProjectsPageResponseDTO> getAgentProjects(final String userSub) {
        return Endpoint.createContract(
                "/api/v1/agent-projects",
                HttpMethod.GET,
                Void.class,
                AgentProjectsPageResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", userSub)
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, AgentProjectDTO> getAgentProject() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}",
                HttpMethod.GET,
                Void.class,
                AgentProjectDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<PatchAgentProjectRequestDTO, AgentProjectDTO> patchAgentProject() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}",
                HttpMethod.PATCH,
                PatchAgentProjectRequestDTO.class,
                AgentProjectDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("patchAgentProjectNameOnlyRequest.json")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, Void> deleteAgentProject() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}",
                HttpMethod.DELETE,
                Void.class,
                Void.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(204)
        );
    }

    public static Endpoint<Void, ProjectAgentsResponseDTO> listAgentProjectAgents() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}/agents",
                HttpMethod.GET,
                Void.class,
                ProjectAgentsResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(200)
        );
    }

    public static Endpoint<AddAgentToProjectRequestDTO, ProjectAgentResponseDTO> addAgentToProject() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}/agents",
                HttpMethod.POST,
                AddAgentToProjectRequestDTO.class,
                ProjectAgentResponseDTO.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .withRequest("addAgentToProjectRequest.json")
                        .expectStatus(200)
        );
    }

    public static Endpoint<Void, Void> removeAgentFromProject() {
        return Endpoint.createContract(
                "/api/v1/agent-projects/{projectId}/agents/{agentId}",
                HttpMethod.DELETE,
                Void.class,
                Void.class,
                (MockmvcDefault) context -> context
                        .header("X-Forge-User-Sub", "1")
                        .expectStatus(204)
        );
    }

    private ControllerEndpoint() {
    }
}
