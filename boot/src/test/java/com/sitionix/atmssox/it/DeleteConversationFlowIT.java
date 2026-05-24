package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.application.usecase.ChatExecutionAsyncProcessor;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@IntegrationTest
class DeleteConversationFlowIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @MockBean
    private ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @Test
    @DisplayName("Should soft delete conversation and hide it from retrieval endpoints")
    void givenActiveConversation_whenDelete_thenSoftDeleteAndHideFromReadEndpoints() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
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
        final UUID conversationId = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault();

        //then
        final ConversationEntity deletedConversation = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation not found"));
        assertThat(deletedConversation.getStatus()).isEqualTo(ConversationStatus.DELETED);

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentConversations())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("Should keep updated timestamp unchanged when deleting already deleted conversation")
    void givenDeletedConversation_whenDeleteAgain_thenKeepUpdatedTimestampUnchanged() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
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
        final UUID conversationId = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getConversationId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault();
        final Instant updatedAtAfterFirstDelete = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault();

        //then
        final ConversationEntity deletedConversation = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation not found"));
        assertThat(deletedConversation.getStatus()).isEqualTo(ConversationStatus.DELETED);
        assertThat(deletedConversation.getUpdatedAt()).isEqualTo(updatedAtAfterFirstDelete);
    }

    @Test
    @DisplayName("Should return not found and keep conversation active when deleting another user conversation")
    void givenAnotherUserConversation_whenDelete_thenReturnNotFoundAndKeepConversationActive() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
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
        final UUID conversationId = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getConversationId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .header("X-Forge-User-Sub", "2")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        final ConversationEntity activeConversation = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation not found"));
        assertThat(activeConversation.getStatus()).isEqualTo(ConversationStatus.ACTIVE);
    }
}
