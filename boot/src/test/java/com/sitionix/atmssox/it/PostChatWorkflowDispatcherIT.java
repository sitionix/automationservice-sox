package com.sitionix.atmssox.it;

import com.sitionix.atmssox.application.usecase.ChatCompletedContext;
import com.sitionix.atmssox.application.usecase.ContextOptimizerPostChatWorkflow;
import com.sitionix.atmssox.application.usecase.RuleSuggestionPostChatWorkflow;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
class PostChatWorkflowDispatcherIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @MockBean
    private RuleSuggestionPostChatWorkflow ruleSuggestionPostChatWorkflow;

    @MockBean
    private ContextOptimizerPostChatWorkflow contextOptimizerPostChatWorkflow;

    @Test
    @DisplayName("Should dispatch all post-chat workflows when chat succeeds")
    void givenSuccessfulChat_whenChatAgent_thenDispatchAllPostChatWorkflows() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("Chat reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();

        //then
        verify(this.ruleSuggestionPostChatWorkflow, times(1)).submit(any(ChatCompletedContext.class));
        verify(this.contextOptimizerPostChatWorkflow, times(1)).submit(any(ChatCompletedContext.class));
    }

    @Test
    @DisplayName("Should keep chat success and continue other workflow when one post-chat workflow fails")
    void givenRuleSuggestionWorkflowFailure_whenChatAgent_thenKeepSuccessPersistMessagesAndRunOtherWorkflow() {
        //given
        doThrow(new IllegalStateException("workflow failure"))
                .when(this.ruleSuggestionPostChatWorkflow)
                .submit(any(ChatCompletedContext.class));
        when(this.openAiChatClient.execute(any())).thenReturn("Chat reply");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .singleElement()
                .assertEntity()
                .getConversationId();

        //then
        verify(this.ruleSuggestionPostChatWorkflow, times(1)).submit(any(ChatCompletedContext.class));
        verify(this.contextOptimizerPostChatWorkflow, times(1)).submit(any(ChatCompletedContext.class));
        final long savedMessages = this.testManager.postgresql()
                .get(ConversationMessageEntity.class)
                .getAll()
                .stream()
                .filter(message -> Objects.equals(message.getConversation().getConversationId(), conversationId))
                .count();
        assertThat(savedMessages).isEqualTo(2L);
    }
}
