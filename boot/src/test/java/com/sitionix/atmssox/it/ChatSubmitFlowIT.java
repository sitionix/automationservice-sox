package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
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

@IntegrationTest
class ChatSubmitFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should return accepted and persist chat execution when submit chat with valid request")
    void givenActiveAgentAndValidMessage_whenSubmitChat_thenReturnAcceptedAndPersistChatExecution() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACCEPTED"))
                .assertDefault();

        //then
        final ChatExecutionEntity execution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getUserId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getRequestMessage(), "Explain clean architecture in simple words."))
                .andExpected(entity -> Objects.nonNull(entity.getExecutionId()))
                .andExpected(entity -> Objects.nonNull(entity.getConversationId()))
                .andExpected(entity -> Objects.nonNull(entity.getCreatedAt()))
                .andExpected(entity -> Objects.nonNull(entity.getStatus()))
                .assertEntity();

        final Instant deadline = Instant.now().plusSeconds(3);
        while (this.testManager.postgresql().get(ChatExecutionEntity.class).singleElement().assertEntity().getCompletedAt() == null
                && Instant.now().isBefore(deadline)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }
        this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getExecutionId(), execution.getExecutionId()))
                .andExpected(entity -> Objects.nonNull(entity.getCompletedAt()))
                .assertEntity();
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
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("  ")));

        //then
        this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .hasSize(0);
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
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .hasSize(0);
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
                .singleElement()
                .assertEntity()
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
        this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return same execution and persist single row when submit chat twice with same idempotency key")
    void givenSameIdempotencyKey_whenSubmitChatTwice_thenReturnSameExecutionAndPersistSingleRow() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        final String idempotencyKey = "chat-submit-idem-001";

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", idempotencyKey)
                .assertDefault();

        final ChatExecutionEntity firstExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .singleElement()
                .assertEntity();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("Idempotency-Key", idempotencyKey)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").value(firstExecution.getExecutionId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").value(firstExecution.getConversationId().toString()))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getExecutionId(), firstExecution.getExecutionId()))
                .andExpected(entity -> Objects.equals(entity.getConversationId(), firstExecution.getConversationId()))
                .assertEntity();

        final Instant deadline = Instant.now().plusSeconds(3);
        while (this.testManager.postgresql().get(ChatExecutionEntity.class).singleElement().assertEntity().getCompletedAt() == null
                && Instant.now().isBefore(deadline)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }
    }
}
