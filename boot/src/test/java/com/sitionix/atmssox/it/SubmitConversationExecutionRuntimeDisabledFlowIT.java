package com.sitionix.atmssox.it;

import com.sitionix.atmssox.application.usecase.ChatExecutionAsyncProcessor;
import com.sitionix.atmssox.application.usecase.ConversationExecutionProperties;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@IntegrationTest
class SubmitConversationExecutionRuntimeDisabledFlowIT {

    @Autowired
    private TestManager testManager;

    @Autowired
    private ConversationExecutionProperties conversationExecutionProperties;

    @MockBean
    private ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @BeforeEach
    void setDisabledRuntimeByDefault() {
        this.conversationExecutionProperties.setRuntimeDispatchEnabled(false);
    }

    @AfterEach
    void resetRuntimeDispatchFlag() {
        this.conversationExecutionProperties.setRuntimeDispatchEnabled(false);
    }

    @Test
    @DisplayName("given runtime dispatch disabled when submit conversation execution then persist user message without execution metadata")
    void givenRuntimeDispatchDisabled_whenSubmitConversationExecution_thenPersistUserMessageWithoutExecutionMetadata() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();

        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        final long initialMessageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        final long initialExecutionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .count();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").value(conversationId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.runtimeDispatched").value(false))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").doesNotExist())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionStatus").doesNotExist())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.startedAt").doesNotExist())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.completedAt").doesNotExist())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("disabled message")));

        //then
        final long messageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        final long executionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .count();
        assertThat(messageCount).isEqualTo(initialMessageCount + 1L);
        assertThat(executionCount).isEqualTo(initialExecutionCount);
        verifyNoInteractions(this.chatExecutionAsyncProcessor);
    }

    @Test
    @DisplayName("given disabled submit when get conversation then show message history without synthetic execution")
    void givenDisabledSubmit_whenGetConversation_thenShowMessageHistoryWithoutSyntheticExecution() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("history message")));

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(conversationId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages[0].authorType").value("USER"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages[0].content").value("history message"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.execution").doesNotExist())
                .assertDefault();

        final List<ChatExecutionEntity> executions = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .toList();
        assertThat(executions).isEmpty();
    }

    @Test
    @DisplayName("given blank message when submit conversation execution then return bad request and persist no side effects")
    void givenBlankMessage_whenSubmitConversationExecution_thenReturnBadRequestAndPersistNoSideEffects() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();
        final long initialMessageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();
        final long initialExecutionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().size();

        //when
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("    ")));

        //then
        assertThat(this.testManager.postgresql().get(ConversationMessageEntity.class).getAll()).hasSize((int) initialMessageCount);
        assertThat(this.testManager.postgresql().get(ChatExecutionEntity.class).getAll()).hasSize((int) initialExecutionCount);
        verifyNoInteractions(this.chatExecutionAsyncProcessor);
    }

    @Test
    @DisplayName("given non owned or deleted conversation when submit then return not found and persist no side effects")
    void givenNonOwnedOrDeletedConversation_whenSubmit_thenReturnNotFoundAndPersistNoSideEffects() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();
        final long initialMessageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();
        final long initialExecutionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().size();

        //when
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .header("X-Forge-User-Sub", "2")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        assertThat(this.testManager.postgresql().get(ConversationMessageEntity.class).getAll()).hasSize((int) initialMessageCount);
        assertThat(this.testManager.postgresql().get(ChatExecutionEntity.class).getAll()).hasSize((int) initialExecutionCount);
        verifyNoInteractions(this.chatExecutionAsyncProcessor);
    }

    @Test
    @DisplayName("given consecutive disabled submits when submit multiple messages then persist messages and keep execution rows unchanged")
    void givenConsecutiveDisabledSubmits_whenSubmitMultipleMessages_thenPersistMessagesAndKeepExecutionRowsUnchanged() {
        //given
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();
        final long initialMessageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        final long initialExecutionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .count();

        //when
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("first disabled message")));
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("second disabled message")));

        //then
        final long messageCount = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        final long executionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .count();
        assertThat(messageCount).isEqualTo(initialMessageCount + 2L);
        assertThat(executionCount).isEqualTo(initialExecutionCount);
        verifyNoInteractions(this.chatExecutionAsyncProcessor);
    }

    @Test
    @DisplayName("given runtime dispatch enabled when submit conversation execution then create execution and trigger async dispatch")
    void givenRuntimeDispatchEnabled_whenSubmitConversationExecution_thenCreateExecutionAndTriggerAsyncDispatch() {
        //given
        this.conversationExecutionProperties.setRuntimeDispatchEnabled(true);
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.runtimeDispatched").value(true))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionStatus").value("ACCEPTED"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("enabled message")));

        //then
        final ChatExecutionEntity execution = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));
        assertThat(execution.getInputMessageId()).isNotNull();
        verify(this.chatExecutionAsyncProcessor, timeout(1000))
                .onChatExecutionSubmitted(argThat(event -> Objects.equals(event.executionId(), execution.getExecutionId())));
    }

    @Test
    @DisplayName("given gate toggled between requests when submit then first stays disabled and second gets execution")
    void givenGateToggledBetweenRequests_whenSubmit_thenFirstStaysDisabledAndSecondGetsExecution() {
        //given
        this.conversationExecutionProperties.setRuntimeDispatchEnabled(false);
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgentProject()).assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc().ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc().ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc().ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql().get(ConversationEntity.class).getAll().stream()
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.runtimeDispatched").value(false))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionId").doesNotExist())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("disabled before toggle")));
        this.conversationExecutionProperties.setRuntimeDispatchEnabled(true);
        this.testManager.mockMvc().ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.runtimeDispatched").value(true))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionStatus").value("ACCEPTED"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage("enabled after toggle")));

        //then
        final long executionCount = this.testManager.postgresql().get(ChatExecutionEntity.class).getAll().stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .count();
        assertThat(executionCount).isEqualTo(1L);
    }
}
