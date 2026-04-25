package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestPropertySource(properties = {
        "automation.rule-suggestion-analyzer.message-count-threshold=1",
        "automation.rule-suggestion-analyzer.conversation-cooldown-minutes=0",
        "automation.rule-suggestion-analyzer.max-agent-analyses-per-day=1"
})
class RuleSuggestionAnalyzerDailyQuotaIT {

    private static final String ANALYZER_INSTRUCTION = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should skip second analyzer run when daily quota is exhausted")
    void givenDailyQuotaReached_whenChatAgentAgain_thenDoNotPersistSecondSuggestion() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemRuleAnalyzerActiveAgent.json"))
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

        final AtomicInteger analyzerInvocation = new AtomicInteger(0);
        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final String instruction = invocation.getArgument(0, OpenAiChatRequest.class).instruction();
                    if (Objects.equals(instruction, ANALYZER_INSTRUCTION)) {
                        final int run = analyzerInvocation.incrementAndGet();
                        return """
                                {"suggestions":[{"title":"Quota %d","content":"Quota content %d","reason":"Quota reason"}]}
                                """.formatted(run, run);
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Quota first"))
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
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() > baselineRuleCount) {
                break;
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Unexpected interruption", exception);
            }
        }

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Quota second");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == baselineRuleCount + 1) {
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Unexpected interruption", exception);
                }
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size()).isEqualTo(baselineRuleCount + 1);
        verify(this.openAiChatClient, times(1)).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)
                && Objects.nonNull(request.input())));
    }
}
