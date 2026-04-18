package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.nullValue;

@IntegrationTest
class PatchAgentFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should patch instruction only and keep other fields unchanged")
    void givenExistingAgent_whenPatchInstructionOnly_thenPersistInstructionAndReturnFullAgent() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentInstructionOnlyRequest.json")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(createdAgent.getAgentId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.instruction").value("Follow security-first code review checklist"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.equals(entity.getInstruction(), "Follow security-first code review checklist"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(createdAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should patch description only and keep name and instruction")
    void givenExistingAgent_whenPatchDescriptionOnly_thenPersistDescriptionAndReturnFullAgent() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentDescriptionOnlyRequest.json")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(createdAgent.getAgentId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Focused context for automation workflows"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.instruction").value(nullValue()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Focused context for automation workflows"))
                .andExpected(entity -> Objects.isNull(entity.getInstruction()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(createdAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should patch name and instruction in single request")
    void givenExistingAgent_whenPatchNameAndInstruction_thenPersistBothFields() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentNameAndInstructionRequest.json")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(createdAgent.getAgentId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Updated Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.instruction").value("Document expected outputs for each run"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Updated Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.equals(entity.getInstruction(), "Document expected outputs for each run"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(createdAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should reject empty patch body and keep row unchanged")
    void givenEmptyPatchBody_whenPatchAgent_thenReturnBadRequestAndKeepDbUnchanged() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final Instant beforePatchUpdatedAt = createdAgent.getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentEmptyRequest.json")
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.isNull(entity.getInstruction()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforePatchUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return instruction in get by id after patch")
    void givenAgentWithInstruction_whenGetAgentById_thenReturnInstructionField() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .withRequest("patchAgentInstructionOnlyRequest.json")
                .assertDefault();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.instruction").value("Follow security-first code review checklist"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return unauthorized and keep row unchanged when user context is missing")
    void givenMissingUserContext_whenPatchAgent_thenReturnUnauthorizedAndKeepDbUnchanged() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final Instant beforePatchUpdatedAt = createdAgent.getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentInstructionOnlyRequest.json")
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.isNull(entity.getInstruction()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforePatchUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return bad request for malformed patch agent id")
    void givenMalformedAgentId_whenPatchAgent_thenReturnBadRequest() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final Instant beforePatchUpdatedAt = createdAgent.getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .withRequest("patchAgentInstructionOnlyRequest.json")
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.isNull(entity.getInstruction()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforePatchUpdatedAt))
                .assertEntity();
    }
}
