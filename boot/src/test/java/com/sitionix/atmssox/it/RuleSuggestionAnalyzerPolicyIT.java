package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
@TestPropertySource(properties = {
        "automation.rule-suggestion-analyzer.message-count-threshold=1"
})
class RuleSuggestionAnalyzerPolicyIT {

    private static final String ANALYZER_INSTRUCTION = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should keep chat success and skip generic analyzer suggestion content")
    void givenAnalyzerReturnsGenericRule_whenChatAgent_thenKeepSuccessAndDoNotPersistSuggestion() {
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

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final String instruction = invocation.getArgument(0, OpenAiChatRequest.class).instruction();
                    if (Objects.equals(instruction, ANALYZER_INSTRUCTION)) {
                        return """
                                {"suggestions":[{"title":"Generic","content":"be helpful","reason":"Generic output"}]}
                                """;
                    }
                    return "Chat reply";
                });

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Generic case"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == baselineRuleCount) {
                java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll()).hasSize(baselineRuleCount);
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)
                && Objects.nonNull(request.input())));
    }

    @Test
    @DisplayName("Should keep chat success and persist no suggestion when analyzer OpenAI call fails")
    void givenAnalyzerOpenAiFailure_whenChatAgent_thenKeepSuccessAndDoNotPersistSuggestion() {
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

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final String instruction = invocation.getArgument(0, OpenAiChatRequest.class).instruction();
                    if (Objects.equals(instruction, ANALYZER_INSTRUCTION)) {
                        throw new OpenAiExecutionException("Analyzer OpenAI failure");
                    }
                    return "Chat reply";
                });

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Failure case"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == baselineRuleCount) {
                java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll()).hasSize(baselineRuleCount);
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)
                && Objects.nonNull(request.input())));
    }

    @Test
    @DisplayName("Should skip second analyzer run when cooldown is not passed")
    void givenFreshAnalyzerSuggestion_whenChatAgainImmediately_thenCooldownBlocksNextSuggestion() {
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

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final String instruction = invocation.getArgument(0, OpenAiChatRequest.class).instruction();
                    if (Objects.equals(instruction, ANALYZER_INSTRUCTION)) {
                        return """
                                {"suggestions":[{"title":"First","content":"First analyzer rule","reason":"First"}]}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Cooldown first"))
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
            java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
        }

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Cooldown second");
                })
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == baselineRuleCount + 1) {
                java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll()).hasSize(baselineRuleCount + 1);
        verify(this.openAiChatClient, times(1)).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)
                && Objects.nonNull(request.input())));
    }

    @Test
    @DisplayName("Should skip analyzer when max pending AI suggestions limit is reached")
    void givenMaxPendingSuggestionsReached_whenChatAgent_thenDoNotCreateAdditionalSuggestion() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("systemRuleAnalyzerActiveAgent.json"))
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi2.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi3.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi4.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi5.json"))
                .build();
        final int baselineRuleCount = this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size();

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenReturn("Chat reply");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111"))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Pending limit case"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == baselineRuleCount) {
                java.util.concurrent.locks.LockSupport.parkNanos(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(20L));
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll()).hasSize(baselineRuleCount);
        verify(this.openAiChatClient, never()).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), ANALYZER_INSTRUCTION)));
    }
}
