package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationContextSnapshotEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestPropertySource(properties = {
        "automation.context-optimizer.last-messages-limit=3",
        "automation.context-optimizer.optimize-threshold-messages=0",
        "automation.context-optimizer.conversation-cooldown-minutes=0",
        "automation.rule-suggestion-analyzer.enabled=false"
})
class ContextOptimizerFlowIT {

    private static final String OPTIMIZER_INSTRUCTION = "Summarize conversation context. Return only strict JSON {\"summary\":\"...\"}.";

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should build prompt without summary when no snapshot exists")
    void givenNoSnapshot_whenChatAgent_thenBuildPromptWithoutSummary() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("patchAgentInstructionOnlyRequest.json", request -> request.setInstruction("  Answer briefly.  "))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.nonNull(request.instruction())
                && request.instruction().contains("Answer briefly.")
                && !request.instruction().contains("Conversation context summary:")
                && Objects.nonNull(request.input())
                && request.input().contains("Messages:")
                && request.input().contains("USER: Explain clean architecture in simple words."))))
                .thenReturn("Reply without summary.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Reply without summary."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.nonNull(request.instruction())
                && request.instruction().contains("Answer briefly.")
                && !request.instruction().contains("Conversation context summary:")
                && Objects.nonNull(request.input())
                && request.input().contains("Messages:")
                && request.input().contains("USER: Explain clean architecture in simple words.")));
    }

    @Test
    @DisplayName("Should include summary and only last N messages in chat prompt")
    void givenExistingSummaryAndLongConversation_whenChatAgent_thenUseSummaryAndLastNOnlyInPrompt() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("patchAgentInstructionOnlyRequest.json", request -> request.setInstruction("Answer briefly."))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        return """
                                {"summary":"Project Alpha uses Spring Boot and Kafka."}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("M1 old fact"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("M2 recent fact");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<ConversationContextSnapshotEntity> snapshots = this.testManager.postgresql()
                    .get(ConversationContextSnapshotEntity.class)
                    .getAll();
            final boolean hasConversationSnapshot = snapshots.stream()
                    .anyMatch(snapshot -> Objects.equals(snapshot.getConversation().getConversationId(), conversationId));
            if (hasConversationSnapshot) {
                break;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        clearInvocations(this.openAiChatClient);

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("M3 latest fact");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.nonNull(request.instruction())
                && request.instruction().contains("Answer briefly.")
                && request.instruction().contains("Conversation context summary:\nProject Alpha uses Spring Boot and Kafka.")
                && Objects.nonNull(request.input())
                && request.input().contains("Messages:")
                && request.input().contains("M2 recent fact")
                && request.input().contains("M3 latest fact")
                && !request.input().contains("M1 old fact")));
    }

    @Test
    @DisplayName("Should create conversation context snapshot after threshold")
    void givenOptimizerAllowed_whenChatAgent_thenCreateSnapshot() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        return """
                                {"summary":"Snapshot summary"}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Message one"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Message two");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        ConversationContextSnapshotEntity snapshot = null;
        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<ConversationContextSnapshotEntity> snapshots = this.testManager.postgresql()
                    .get(ConversationContextSnapshotEntity.class)
                    .getAll();
            snapshot = snapshots.stream()
                    .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(snapshot)) {
                break;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        //then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getConversation().getConversationId()).isEqualTo(conversationId);
        assertThat(snapshot.getSummary()).isEqualTo("Snapshot summary");
        assertThat(snapshot.getMessageCountUntil()).isGreaterThan(0);
        assertThat(snapshot.getLastMessageIdUntil()).isNotNull();
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)
                && Objects.nonNull(request.input())
                && request.input().contains("Messages to summarize:")));
    }

    @Test
    @DisplayName("Should update existing snapshot instead of creating duplicate row")
    void givenExistingSnapshotAndNewMessages_whenChatAgent_thenUpdateSameSnapshotRow() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        if (Objects.nonNull(request.input()) && request.input().contains("Message three")) {
                            return """
                                    {"summary":"summary-after-three"}
                                    """;
                        }
                        return """
                                {"summary":"summary-after-two"}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Message one"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Message two");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        ConversationContextSnapshotEntity initialSnapshot = null;
        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<ConversationContextSnapshotEntity> snapshots = this.testManager.postgresql()
                    .get(ConversationContextSnapshotEntity.class)
                    .getAll();
            initialSnapshot = snapshots.stream()
                    .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(initialSnapshot)
                    && Objects.equals(initialSnapshot.getSummary(), "summary-after-two")) {
                break;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        assertThat(initialSnapshot).isNotNull();
        final UUID snapshotId = initialSnapshot.getId();
        final Integer messageCountUntil = initialSnapshot.getMessageCountUntil();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Message three");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        ConversationContextSnapshotEntity updatedSnapshot = null;
        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<ConversationContextSnapshotEntity> snapshots = this.testManager.postgresql()
                    .get(ConversationContextSnapshotEntity.class)
                    .getAll();
            final ConversationContextSnapshotEntity snapshot = snapshots.stream()
                    .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(snapshot)
                    && Objects.equals(snapshot.getSummary(), "summary-after-three")
                    && snapshot.getMessageCountUntil() > messageCountUntil) {
                updatedSnapshot = snapshot;
                break;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        //then
        assertThat(updatedSnapshot).isNotNull();
        assertThat(updatedSnapshot.getId()).isEqualTo(snapshotId);
        assertThat(updatedSnapshot.getSummary()).isEqualTo("summary-after-three");
        assertThat(updatedSnapshot.getMessageCountUntil()).isGreaterThan(messageCountUntil);
        final long conversationSnapshots = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        assertThat(conversationSnapshots).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should not run optimizer below threshold")
    void givenConversationBelowThreshold_whenChatAgent_thenSkipOptimizerAndDoNotCreateSnapshot() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenReturn("Chat reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Below threshold"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int attempt = 0; attempt < 50; attempt++) {
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(10L));
        }

        //then
        final long conversationSnapshots = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        assertThat(conversationSnapshots).isEqualTo(0L);
        verify(this.openAiChatClient, never()).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)));
    }

    @Test
    @DisplayName("Should keep chat success and skip snapshot update when optimizer returns invalid JSON")
    void givenOptimizerReturnsInvalidJson_whenChatAgent_thenKeepSuccessAndSkipSnapshotUpdate() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        return "not-json";
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Message one"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Message two");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 250; attempt++) {
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        //then
        final long conversationSnapshots = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        assertThat(conversationSnapshots).isEqualTo(0L);
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)));
    }

    @Test
    @DisplayName("Should keep chat success and skip optimizer when system context optimizer agent is missing")
    void givenMissingSystemContextOptimizer_whenChatAgent_thenKeepSuccessAndDoNotCreateSnapshot() {
        //given
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenReturn("Chat reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID userAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getType().getId(), 1L))
                .max(java.util.Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("User agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Message one"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Message two");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 80; attempt++) {
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(10L));
        }

        //then
        final long conversationSnapshots = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .count();
        assertThat(conversationSnapshots).isEqualTo(0L);
    }
}
