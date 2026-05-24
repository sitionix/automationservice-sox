package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import com.sitionix.forgeit.mockmvc.api.QueryParams;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestPropertySource(properties = {
        "automation.rule-suggestion-analyzer.enabled=false",
        "automation.context-optimizer.optimize-threshold-messages=1000000"
})
class ChatExecutionRetrievalFlowIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should submit chat by executions path and persist execution")
    void givenActiveAgent_whenSubmitByExecutionsPath_thenReturnAcceptedAndPersistExecution() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .assertDefault();

        //then
        final long executionCount = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .count();
        assertThat(executionCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return chat execution by execution id")
    void givenExecutionExists_whenGetExecution_thenReturnExecutionProjection() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("executionId", execution.getExecutionId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").value(execution.getExecutionId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").value(execution.getConversationId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").exists())
                .assertDefault();
    }

    @Test
    @DisplayName("Should return forbidden when execution belongs to another user")
    void givenExecutionOfAnotherUser_whenGetExecution_thenReturnForbidden() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("executionId", execution.getExecutionId()))
                .header("X-Forge-User-Sub", "2")
                .expectStatus(HttpStatus.FORBIDDEN)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return not found when execution id does not exist")
    void givenUnknownExecutionId_whenGetExecution_thenReturnNotFound() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("executionId", UUID.randomUUID()))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return accepted in retrieval right after submit")
    void givenJustSubmittedExecution_whenGetExecutionImmediately_thenReturnAcceptedState() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("executionId", execution.getExecutionId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request when submit by executions path with blank message")
    void givenBlankMessage_whenSubmitByExecutionsPath_thenReturnBadRequest() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("   ")));
    }

    @Test
    @DisplayName("Should return unauthorized when submit by executions path without user context")
    void givenMissingUserContext_whenSubmitByExecutionsPath_thenReturnUnauthorized() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should accept submit by executions path for draft agent")
    void givenDraftAgent_whenSubmitByExecutionsPath_thenReturnAccepted() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.ACCEPTED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should accept submit by executions path for archived agent")
    void givenArchivedAgent_whenSubmitByExecutionsPath_thenReturnAccepted() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.ACCEPTED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request when submit by executions path twice with same idempotency key")
    void givenSameIdempotencyKey_whenSubmitByExecutionsPathTwice_thenReturnBadRequest() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", "idem-exec-path-1")
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", "idem-exec-path-1")
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return unauthorized when get execution without user context")
    void givenMissingUserContext_whenGetExecution_thenReturnUnauthorized() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final UUID executionId = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getExecutionId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("executionId", executionId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return not found when execution exists but another agent id is used")
    void givenExecutionExistsForAnotherAgent_whenGetExecution_thenReturnNotFound() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID firstAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .sorted(Comparator.comparing(AgentEntity::getCreatedAt).reversed())
                .skip(1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("First agent not found"))
                .getAgentId();
        final UUID secondAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Second agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", firstAgentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", firstAgentId))
                .assertDefault();

        final UUID executionId = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), firstAgentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getExecutionId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", secondAgentId).add("executionId", executionId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request when agent id path param is malformed for get execution")
    void givenMalformedAgentId_whenGetExecution_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", "%%%").add("executionId", UUID.randomUUID()))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request when execution id path param is malformed for get execution")
    void givenMalformedExecutionId_whenGetExecution_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", UUID.randomUUID()).add("executionId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return ok when conversation id query matches execution conversation")
    void givenMatchingConversationId_whenGetExecution_thenReturnOk() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("executionId", execution.getExecutionId()))
                .withQueryParameters(QueryParams.create().add("conversationId", execution.getConversationId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").value(execution.getExecutionId().toString()))
                .assertDefault();
    }

    @ParameterizedTest
    @ValueSource(strings = {"%%%", "not-uuid", "123", "x", "-", "----", "null", "{}", "[]", "abc-def"})
    @DisplayName("Should return bad request for malformed execution id path values")
    void givenMalformedExecutionIds_whenGetExecution_thenReturnBadRequest(final String malformedExecutionId) {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", UUID.randomUUID()).add("executionId", malformedExecutionId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @ParameterizedTest
    @ValueSource(strings = {"%%%", "not-uuid", "123", "x", "-", "----", "null", "{}", "[]", "abc-def"})
    @DisplayName("Should return bad request for malformed agent id path values on retrieval")
    void givenMalformedAgentIds_whenGetExecution_thenReturnBadRequest(final String malformedAgentId) {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", malformedAgentId).add("executionId", UUID.randomUUID()))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request when submit by executions path with malformed agent id")
    void givenMalformedAgentId_whenSubmitByExecutionsPath_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should trim idempotency key in submit by executions path")
    void givenIdempotencyKeyWithSpaces_whenSubmitByExecutionsPath_thenTrimAndPersist() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", "  trim-key-1  ")
                .assertDefault();

        //then
        final ChatExecutionEntity execution = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));
        assertThat(execution.getIdempotencyKey()).isEqualTo("trim-key-1");
    }

    @Test
    @DisplayName("Should keep null idempotency key when header not provided in executions path")
    void givenNoIdempotencyHeader_whenSubmitByExecutionsPath_thenPersistNullIdempotencyKey() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //then
        final ChatExecutionEntity execution = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));
        assertThat(execution.getIdempotencyKey()).isNull();
    }

    @Test
    @DisplayName("Should return bad request when get execution with malformed conversation id query")
    void givenMalformedConversationIdQuery_whenGetExecution_thenReturnBadRequest() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        final UUID executionId = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getExecutionId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("executionId", executionId))
                .withQueryParameters(QueryParams.create().add("conversationId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return accepted when get execution after submit by executions path")
    void givenSubmitByExecutionsPath_whenGetExecution_thenReturnAccepted() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.chatAgentByExecutionsPath())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        final ChatExecutionEntity execution = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentChatExecution())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("executionId", execution.getExecutionId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .assertDefault();
    }
}
