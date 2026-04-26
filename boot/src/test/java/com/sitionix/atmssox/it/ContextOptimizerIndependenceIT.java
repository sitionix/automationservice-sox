package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationContextSnapshotEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
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
import static org.mockito.Mockito.when;

@IntegrationTest
@TestPropertySource(properties = {
        "automation.context-optimizer.last-messages-limit=1",
        "automation.context-optimizer.optimize-threshold-messages=0",
        "automation.context-optimizer.conversation-cooldown-minutes=0",
        "automation.rule-suggestion-analyzer.message-count-threshold=1",
        "automation.rule-suggestion-analyzer.conversation-cooldown-minutes=0",
        "automation.rule-suggestion-analyzer.max-agent-analyses-per-day=10",
        "automation.rule-suggestion-analyzer.max-pending-suggestions-per-agent=10"
})
class ContextOptimizerIndependenceIT {

    private static final String OPTIMIZER_INSTRUCTION = "Summarize conversation context. Return only strict JSON {\"summary\":\"...\"}.";
    private static final String ANALYZER_INSTRUCTION = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should keep chat success and persist optimizer snapshot when analyzer fails")
    void givenAnalyzerFailsButOptimizerSucceeds_whenChatAgent_thenChatSuccessAndSnapshotPersists() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemRuleAnalyzerActiveAgent.json"))
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();
        final int baselineRuleCount = this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size();

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

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)) {
                        throw new OpenAiExecutionException("Analyzer failure");
                    }
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        return """
                                {"summary":"Optimizer summary"}
                                """;
                    }
                    return "Chat reply";
                });

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Independence message"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();
        ConversationContextSnapshotEntity snapshot = null;
        for (int attempt = 0; attempt < 250; attempt++) {
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
        assertThat(snapshot.getSummary()).isEqualTo("Optimizer summary");
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll()).hasSize(baselineRuleCount);
    }

    @Test
    @DisplayName("Should keep chat success and persist analyzer suggestion when optimizer fails")
    void givenOptimizerFailsButAnalyzerSucceeds_whenChatAgent_thenChatSuccessAndAnalyzerPersists() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemRuleAnalyzerActiveAgent.json"))
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();
        final int baselineRuleCount = this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size();

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

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)) {
                        return """
                                {"suggestions":[{"title":"Analyzer title","content":"Analyzer content","reason":"Analyzer reason"}]}
                                """;
                    }
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        throw new OpenAiExecutionException("Optimizer failure");
                    }
                    return "Chat reply";
                });

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Independence message"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int attempt = 0; attempt < 150; attempt++) {
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size()).isGreaterThanOrEqualTo(baselineRuleCount);
        final boolean hasConversationSnapshot = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .anyMatch(snapshot -> Objects.equals(snapshot.getConversation().getConversationId(), conversationId));
        assertThat(hasConversationSnapshot).isFalse();
    }
}
