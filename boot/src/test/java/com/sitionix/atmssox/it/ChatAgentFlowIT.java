package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.exception.OpenAiExecutionException;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationParticipantEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@IntegrationTest
class ChatAgentFlowIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should execute chat for active agent and return provider reply")
    void givenActiveAgentAndValidMessage_whenChatAgent_thenReturnReplyAndKeepDbState() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity createdAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("patchAgentInstructionOnlyRequest.json", request ->
                        request.setInstruction("  Follow security-first code review checklist  "))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .assertDefault();

        final AgentEntity activeAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        when(this.openAiChatClient.execute(
                "Follow security-first code review checklist",
                "Conversation history:\nUSER: Explain clean architecture in simple words.\nRespond as AGENT to the latest USER message."))
                .thenReturn("Clean architecture separates core business rules from frameworks.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", createdAgent.getAgentId()))
                .withRequest("chatAgentRequest.json", request ->
                        request.setMessage("  Explain clean architecture in simple words.  "))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content")
                        .value("Clean architecture separates core business rules from frameworks."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq("Follow security-first code review checklist"),
                eq("Conversation history:\nUSER: Explain clean architecture in simple words.\nRespond as AGENT to the latest USER message."));

        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), createdAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getInstruction(), "Follow security-first code review checklist"))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should include only active rules in chat context prompt")
    void givenMixedRuleStatuses_whenChatAgent_thenBuildPromptWithOnlyActiveRules() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .withRequest("patchAgentInstructionOnlyRequest.json", request ->
                        request.setInstruction("  Follow only active rules  "))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setTitle("Deleted rule title");
                    request.setContent("Deleted rule content");
                }));

        final UUID deletedRuleId = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .getAll()
                .stream()
                .filter(rule -> Objects.equals(rule.getTitle(), "Deleted rule title"))
                .map(AgentRuleEntity::getRuleId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Deleted rule not found"));

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("ruleId", deletedRuleId))
                .assertDefault();

        when(this.openAiChatClient.execute(
                eq("Follow only active rules"),
                argThat(context -> Objects.nonNull(context)
                        && context.contains("Active rules:")
                        && context.contains("- Always produce deterministic output")
                        && !context.contains("Deleted AI title")
                        && !context.contains("Deleted rule title")
                        && context.contains("USER: Explain clean architecture in simple words."))))
                .thenReturn("Only active rules were applied.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Only active rules were applied."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq("Follow only active rules"),
                argThat(context -> Objects.nonNull(context)
                        && context.contains("Active rules:")
                        && context.contains("- Always produce deterministic output")
                        && !context.contains("Deleted rule title")
                        && context.contains("USER: Explain clean architecture in simple words.")));
    }

    @Test
    @DisplayName("Should exclude pending rejected and deleted rules from chat context prompt")
    void givenPersistedMixedRuleStatuses_whenChatAgent_thenBuildPromptWithOnlyActiveRules() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveUser.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleRejectedAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleDeletedAi.json"))
                .build();

        when(this.openAiChatClient.execute(
                eq("Follow only active rules"),
                argThat(context -> Objects.nonNull(context)
                        && context.contains("Active rules:")
                        && context.contains("- Active USER content")
                        && context.contains("- Active AI content")
                        && !context.contains("Pending AI title")
                        && !context.contains("Rejected AI title")
                        && !context.contains("Deleted AI title")
                        && context.contains("USER: Explain clean architecture in simple words."))))
                .thenReturn("Only active persisted rules were applied.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Only active persisted rules were applied."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq("Follow only active rules"),
                argThat(context -> Objects.nonNull(context)
                        && context.contains("Active rules:")
                        && context.contains("- Active USER content")
                        && context.contains("- Active AI content")
                        && !context.contains("Pending AI title")
                        && !context.contains("Rejected AI title")
                        && !context.contains("Deleted AI title")
                        && context.contains("USER: Explain clean architecture in simple words.")));
    }

    @Test
    @DisplayName("Should return bad request and not call provider for blank message")
    void givenBlankMessage_whenChatAgent_thenReturnBadRequestAndDoNotCallProvider() {
        //given
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

        final AgentEntity beforeChat = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setMessage("   ")));

        //then
        verifyNoInteractions(this.openAiChatClient);
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeChat.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and not call provider when user context is missing")
    void givenMissingUserContext_whenChatAgent_thenReturnUnauthorizedAndDoNotCallProvider() {
        //given
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

        final AgentEntity beforeChat = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        verifyNoInteractions(this.openAiChatClient);
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeChat.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return not found and not call provider when agent belongs to another user")
    void givenAgentOwnedByAnotherUser_whenChatAgent_thenReturnNotFoundAndDoNotCallProvider() {
        //given
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

        final AgentEntity beforeChat = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", "2")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        verifyNoInteractions(this.openAiChatClient);
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeChat.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return bad request and not call provider when message is missing")
    void givenMissingMessage_whenChatAgent_thenReturnBadRequestAndDoNotCallProvider() {
        //given
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
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setMessage(null)));

        //then
        verifyNoInteractions(this.openAiChatClient);
    }

    @Test
    @DisplayName("Should return bad request and not call provider for malformed agent id")
    void givenMalformedAgentId_whenChatAgent_thenReturnBadRequestAndDoNotCallProvider() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final Instant beforeChatUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        verifyNoInteractions(this.openAiChatClient);
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeChatUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and not call provider when agent is not active")
    void givenDraftAgent_whenChatAgent_thenReturnConflictAndDoNotCallProvider() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity draftAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        verifyNoInteractions(this.openAiChatClient);
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), draftAgent.getAgentId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), draftAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return upstream quota error details and keep only user-side conversation records")
    void givenOpenAiQuotaError_whenChatAgent_thenReturnUpstreamErrorAndKeepOnlyUserSideConversationRecords() {
        //given
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

        final AgentEntity activeAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final int beforeConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int beforeParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int beforeMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();

        when(this.openAiChatClient.execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))))
                .thenThrow(new OpenAiExecutionException(
                        429,
                        "insufficient_quota",
                        "insufficient_quota",
                        "You exceeded your current quota."
                ));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.TOO_MANY_REQUESTS)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(429))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("insufficient_quota"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("You exceeded your current quota."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))
        );
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeAgent.getUpdatedAt()))
                .assertEntity();
        final int afterConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int afterParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int afterMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();
        assertThat(afterConversationSize).isEqualTo(beforeConversationSize);
        assertThat(afterParticipantSize).isEqualTo(beforeParticipantSize);
        assertThat(afterMessageSize).isEqualTo(beforeMessageSize);
    }

    @Test
    @DisplayName("Should return upstream auth error details and keep only user-side conversation records")
    void givenOpenAiInvalidApiKeyError_whenChatAgent_thenReturnUpstreamErrorAndKeepOnlyUserSideConversationRecords() {
        //given
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

        final AgentEntity activeAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final int beforeConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int beforeParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int beforeMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();

        when(this.openAiChatClient.execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))))
                .thenThrow(new OpenAiExecutionException(
                        401,
                        null,
                        "invalid_api_key",
                        "Incorrect API key provided."
                ));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(401))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("invalid_api_key"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("Incorrect API key provided."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))
        );
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeAgent.getUpdatedAt()))
                .assertEntity();
        final int afterConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int afterParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int afterMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();
        assertThat(afterConversationSize).isEqualTo(beforeConversationSize);
        assertThat(afterParticipantSize).isEqualTo(beforeParticipantSize);
        assertThat(afterMessageSize).isEqualTo(beforeMessageSize);
    }

    @Test
    @DisplayName("Should return bad gateway and keep only user-side conversation records when provider fails")
    void givenProviderFailure_whenChatAgent_thenReturnBadGatewayAndKeepDbState() {
        //given
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

        final AgentEntity activeAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final int beforeConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int beforeParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int beforeMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();

        when(this.openAiChatClient.execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))))
                .thenThrow(new OpenAiExecutionException("OpenAI request failed"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.BAD_GATEWAY)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(502))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value(nullValue()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("OpenAI request failed"))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(
                eq(""),
                argThat(context -> Objects.nonNull(context) && context.contains("Explain clean architecture in simple words."))
        );
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeAgent.getUpdatedAt()))
                .assertEntity();
        final int afterConversationSize = this.testManager.postgresql().get(ConversationEntity.class).getAll().size();
        final int afterParticipantSize = this.testManager.postgresql().get(ConversationParticipantEntity.class).getAll().size();
        final int afterMessageSize = this.testManager.postgresql().get(ConversationMessageEntity.class).getAll().size();
        assertThat(afterConversationSize).isEqualTo(beforeConversationSize);
        assertThat(afterParticipantSize).isEqualTo(beforeParticipantSize);
        assertThat(afterMessageSize).isEqualTo(beforeMessageSize);
    }
}
