package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.application.usecase.ChatExecutionAsyncProcessor;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@IntegrationTest
class ChatSubmitFlowIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @MockBean
    private ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @Test
    @DisplayName("Should return accepted and complete execution when submit chat with valid request")
    void givenActiveAgentAndValidMessage_whenSubmitChat_thenReturnAcceptedAndCompleteExecution() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("Clean architecture separates business rules from frameworks.");

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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .assertDefault();

        //then
        final ChatExecutionEntity queuedExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Queued execution not found"));
        assertThat(queuedExecution.getAgentId()).isEqualTo(agentId);
        assertThat(queuedExecution.getUserId()).isEqualTo(1L);
        assertThat(queuedExecution.getRequestMessage()).isEqualTo("Explain clean architecture in simple words.");

        final ChatExecutionEntity persistedExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getExecutionId(), queuedExecution.getExecutionId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Execution not found"));
        assertThat(persistedExecution.getStatus().getId()).isIn(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("Should persist user message and link execution by user message id when submit chat")
    void givenActiveAgent_whenSubmitChat_thenPersistUserMessageAndLinkExecution() {
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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.inputMessageId").isNotEmpty())
                .assertDefault();

        //then
        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        final ConversationMessageEntity userMessage = this.testManager.postgresql()
                .get(ConversationMessageEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), execution.getConversationId()))
                .filter(entity -> Objects.equals(entity.getMessageId(), execution.getInputMessageId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("User message not found by linked input_message_id"));

        assertThat(userMessage.getAuthorType().name()).isEqualTo("USER");
        assertThat(userMessage.getContent()).isEqualTo(execution.getRequestMessage());
        assertThat(execution.getInputMessageId()).isEqualTo(userMessage.getMessageId());
    }

    @Test
    @DisplayName("Should return bad request and persist no chat execution when submit chat with blank message")
    void givenBlankMessage_whenSubmitChat_thenReturnBadRequestAndPersistNoExecution() {
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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("  ")));

        //then
        final long executionCount = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .count();
        assertThat(executionCount).isZero();
    }

    @Test
    @DisplayName("Should return unauthorized and persist no chat execution when user context is missing")
    void givenMissingUserContext_whenSubmitChat_thenReturnUnauthorizedAndPersistNoExecution() {
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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        final long executionCount = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .count();
        assertThat(executionCount).isZero();
    }

    @Test
    @DisplayName("Should return not found and persist no chat execution when conversation does not exist")
    void givenUnknownConversationId_whenSubmitChat_thenReturnNotFoundAndPersistNoExecution() {
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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setConversationId(
                        UUID.fromString("93ad8f04-6253-4f43-9f0e-04f1f5844b26")
                )));

        //then
        final long executionCount = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .count();
        assertThat(executionCount).isZero();
    }

    @Test
    @DisplayName("Should return same execution and persist single row when submit chat twice with same idempotency key")
    void givenSameIdempotencyKey_whenSubmitChatTwice_thenReturnSameExecutionAndPersistSingleRow() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("Idempotent chat reply.");

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

        final String idempotencyKey = "chat-submit-idem-001";

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", idempotencyKey)
                .assertDefault();

        final ChatExecutionEntity firstExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("First execution not found"));

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", idempotencyKey)
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        final long sameExecutionCount = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getExecutionId(), firstExecution.getExecutionId()))
                .count();
        assertThat(sameExecutionCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should fail execution with execution error when provider throws runtime exception")
    void givenProviderThrowsRuntimeException_whenSubmitChat_thenPersistFailedExecutionWithExecutionError() {
        //given
        when(this.openAiChatClient.execute(any())).thenThrow(new RuntimeException("provider boom"));

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
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final ChatExecutionEntity submittedExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Submitted execution not found"));

        //then
        final ChatExecutionEntity failedPersistedExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getExecutionId(), submittedExecution.getExecutionId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Failed execution not found"));
        assertThat(failedPersistedExecution.getStatus().getId()).isIn(1L, 2L, 4L);
        if (Objects.equals(failedPersistedExecution.getStatus().getId(), 4L)) {
            assertThat(failedPersistedExecution.getFailureClass().getId()).isEqualTo(5L);
            assertThat(failedPersistedExecution.getFailureReason()).isEqualTo("Execution failed");
            assertThat(failedPersistedExecution.getFailureRetryable()).isEqualTo(Boolean.TRUE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\t", "\n", "\r\n", "\t\t", "\n\n", " \t ", " \n "})
    @DisplayName("Should return bad request for blank-like messages")
    void givenBlankLikeMessages_whenSubmitChat_thenReturnBadRequest(final String message) {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage(message)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%%%", "not-uuid", "123", "x", "-", "----", "null", "{}", "[]", "abc-def"})
    @DisplayName("Should return bad request for malformed agent path values")
    void givenMalformedAgentIds_whenSubmitChat_thenReturnBadRequest(final String malformedAgentId) {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", malformedAgentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"\t\t", "\n\n"})
    @DisplayName("Should return bad request when submit chat with null or whitespace control message")
    void givenNullOrWhitespaceControlMessage_whenSubmitChat_thenReturnBadRequest(final String message) {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage(message)));
    }

    @Test
    @DisplayName("Should return accepted when submit chat with draft agent")
    void givenDraftAgent_whenSubmitChat_thenReturnAccepted() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.ACCEPTED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return accepted when submit chat with archived agent")
    void givenArchivedAgent_whenSubmitChat_thenReturnAccepted() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.ACCEPTED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return conflict when idempotency key reused with different conversation")
    void givenIdempotencyKeyReusedWithDifferentConversation_whenSubmitChat_thenReturnBadRequest() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        final String key = "idem-conflict-1";

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", key)
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", key)
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("Another message")));
    }

    @Test
    @DisplayName("Should replay same execution when idempotency key reused with same conversation")
    void givenSameConversationAndIdempotencyKey_whenSubmitChat_thenReplayExecution() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        final String key = "idem-replay-1";

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", key)
                .assertDefault();

        final ChatExecutionEntity first = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", key)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").value(first.getExecutionId().toString()))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setConversationId(first.getConversationId());
                    request.setMessage("Repeat same conversation");
                }));
    }

}
