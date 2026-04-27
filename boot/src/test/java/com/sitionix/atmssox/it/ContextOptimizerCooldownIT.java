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
import java.util.concurrent.atomic.AtomicInteger;
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
        "automation.context-optimizer.last-messages-limit=2",
        "automation.context-optimizer.optimize-threshold-messages=0",
        "automation.context-optimizer.conversation-cooldown-minutes=60",
        "automation.rule-suggestion-analyzer.enabled=false"
})
class ContextOptimizerCooldownIT {

    private static final String OPTIMIZER_INSTRUCTION = "Summarize conversation context. Return only strict JSON {\"summary\":\"...\"}.";

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should skip optimizer rerun when cooldown is not passed")
    void givenFreshSnapshotWithinCooldown_whenChatAgentAgain_thenDoNotRunOptimizer() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemContextOptimizerActiveAgent.json"))
                .build();

        final AtomicInteger optimizerInvocations = new AtomicInteger(0);
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final OpenAiChatRequest request = invocation.getArgument(0, OpenAiChatRequest.class);
                    if (Objects.equals(request.instruction(), OPTIMIZER_INSTRUCTION)) {
                        final int count = optimizerInvocations.incrementAndGet();
                        return """
                                {"summary":"cooldown-summary-%d"}
                                """.formatted(count);
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

        ConversationContextSnapshotEntity snapshotAfterFirstRun = null;
        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<ConversationContextSnapshotEntity> snapshots = this.testManager.postgresql()
                    .get(ConversationContextSnapshotEntity.class)
                    .getAll();
            snapshotAfterFirstRun = snapshots.stream()
                    .filter(snapshot -> Objects.equals(snapshot.getConversation().getConversationId(), conversationId))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(snapshotAfterFirstRun) && optimizerInvocations.get() == 1) {
                break;
            }
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        assertThat(snapshotAfterFirstRun).isNotNull();
        final Integer messageCountAfterFirstRun = snapshotAfterFirstRun.getMessageCountUntil();

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

        for (int attempt = 0; attempt < 120; attempt++) {
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(10L));
        }

        //then
        assertThat(optimizerInvocations.get()).isEqualTo(1);
        final ConversationContextSnapshotEntity snapshotAfterSecondChat = this.testManager.postgresql()
                .get(ConversationContextSnapshotEntity.class)
                .getAll()
                .stream()
                .filter(snapshot -> Objects.equals(snapshot.getConversation().getConversationId(), conversationId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation snapshot not found"));
        assertThat(snapshotAfterSecondChat.getMessageCountUntil()).isEqualTo(messageCountAfterFirstRun);
        assertThat(snapshotAfterSecondChat.getSummary()).isEqualTo("cooldown-summary-1");
    }
}
