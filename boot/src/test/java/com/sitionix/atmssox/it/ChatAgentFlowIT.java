package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
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

        when(this.openAiChatClient.execute(eq(new OpenAiChatRequest(
                "Follow security-first code review checklist",
                "Conversation history:\nUSER: Explain clean architecture in simple words.\nRespond as AGENT to the latest USER message."
        ))))
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
        verify(this.openAiChatClient).execute(eq(new OpenAiChatRequest(
                "Follow security-first code review checklist",
                "Conversation history:\nUSER: Explain clean architecture in simple words.\nRespond as AGENT to the latest USER message."
        )));

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

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "Follow only active rules")
                && Objects.nonNull(request.input())
                && request.input().contains("Active rules:")
                && request.input().contains("- Always produce deterministic output")
                && !request.input().contains("Deleted AI title")
                && !request.input().contains("Deleted rule title")
                && request.input().contains("USER: Explain clean architecture in simple words."))))
                .thenReturn("Only active rules were applied.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Only active rules were applied."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "Follow only active rules")
                && Objects.nonNull(request.input())
                && request.input().contains("Active rules:")
                && request.input().contains("- Always produce deterministic output")
                && !request.input().contains("Deleted rule title")
                && request.input().contains("USER: Explain clean architecture in simple words.")));
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

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "Follow only active rules")
                && Objects.nonNull(request.input())
                && request.input().contains("Active rules:")
                && request.input().contains("- Active USER content")
                && request.input().contains("- Active AI content")
                && !request.input().contains("Pending AI title")
                && !request.input().contains("Rejected AI title")
                && !request.input().contains("Deleted AI title")
                && request.input().contains("USER: Explain clean architecture in simple words."))))
                .thenReturn("Only active persisted rules were applied.");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Only active persisted rules were applied."))
                .assertDefault();

        //then
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "Follow only active rules")
                && Objects.nonNull(request.input())
                && request.input().contains("Active rules:")
                && request.input().contains("- Active USER content")
                && request.input().contains("- Active AI content")
                && !request.input().contains("Pending AI title")
                && !request.input().contains("Rejected AI title")
                && !request.input().contains("Deleted AI title")
                && request.input().contains("USER: Explain clean architecture in simple words.")));
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

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words."))))
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
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words.")));
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

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words."))))
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
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words.")));
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

        when(this.openAiChatClient.execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words."))))
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
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), "")
                && Objects.nonNull(request.input())
                && request.input().contains("Explain clean architecture in simple words.")));
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
    @DisplayName("Should create pending AI suggestion after threshold when analyzer returns valid JSON")
    void givenAnalyzerPresentAndThresholdReached_whenChatAgent_thenPersistPendingAiSuggestionFromAnalyzerJson() {
        //given
        final String analyzerInstruction = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";
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
                    if (Objects.equals(instruction, analyzerInstruction)) {
                        return """
                                {"suggestions":[{"title":"Language preference","content":"Always answer in Ukrainian unless explicitly asked otherwise.","reason":"User repeatedly requested Ukrainian language."}]}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Message 1"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int index = 2; index <= 10; index++) {
            final int messageNumber = index;
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.chatAgent())
                    .withPathParameters(PathParams.create().add("agentId", userAgentId))
                    .withRequest("chatAgentRequest.json", request -> {
                        request.setConversationId(conversationId);
                        request.setMessage("Message " + messageNumber);
                    })
                    .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                    .assertDefault();
        }

        AgentRuleEntity persistedSuggestion = null;
        for (int attempt = 0; attempt < 150; attempt++) {
            final java.util.List<AgentRuleEntity> allRules = this.testManager.postgresql()
                    .get(AgentRuleEntity.class)
                    .getAll();
            if (allRules.size() > baselineRuleCount) {
                persistedSuggestion = allRules.stream()
                        .max(java.util.Comparator.comparing(AgentRuleEntity::getCreatedAt))
                        .orElse(null);
                break;
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Unexpected interruption", exception);
            }
        }

        //then
        assertThat(persistedSuggestion).isNotNull();
        assertThat(persistedSuggestion.getStatus().getId()).isEqualTo(3L);
        assertThat(persistedSuggestion.getAuthorType().getId()).isEqualTo(2L);
        assertThat(persistedSuggestion.getTitle()).isEqualTo("Language preference");
        assertThat(persistedSuggestion.getContent()).isEqualTo("Always answer in Ukrainian unless explicitly asked otherwise.");
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), analyzerInstruction)
                && Objects.nonNull(request.input())
                && request.input().contains("Conversation:")
                && request.input().contains("Latest user message")));
    }

    @Test
    @DisplayName("Should keep chat success and persist no suggestions when analyzer returns malformed JSON")
    void givenAnalyzerReturnsMalformedJson_whenChatAgent_thenKeepChatSuccessAndDoNotPersistSuggestion() {
        //given
        final String analyzerInstruction = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";
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
                    if (Objects.equals(instruction, analyzerInstruction)) {
                        return "not-json";
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Malformed message 1"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int index = 2; index <= 10; index++) {
            final int messageNumber = index;
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.chatAgent())
                    .withPathParameters(PathParams.create().add("agentId", userAgentId))
                    .withRequest("chatAgentRequest.json", request -> {
                        request.setConversationId(conversationId);
                        request.setMessage("Malformed message " + messageNumber);
                    })
                    .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                    .assertDefault();
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().isEmpty()) {
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Unexpected interruption", exception);
                }
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size()).isEqualTo(baselineRuleCount);
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), analyzerInstruction)
                && Objects.nonNull(request.input())
                && request.input().contains("Latest user message")));
    }

    @Test
    @DisplayName("Should keep chat success and persist no suggestions when analyzer returns wrong payload shape")
    void givenAnalyzerReturnsInvalidSuggestionsPayload_whenChatAgent_thenKeepChatSuccessAndDoNotPersistSuggestion() {
        //given
        final String analyzerInstruction = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";
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
                    if (Objects.equals(instruction, analyzerInstruction)) {
                        return "{\"suggestions\":{\"title\":\"title\",\"content\":\"content\",\"reason\":\"reason\"}}";
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Invalid payload 1"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int index = 2; index <= 10; index++) {
            final int messageNumber = index;
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.chatAgent())
                    .withPathParameters(PathParams.create().add("agentId", userAgentId))
                    .withRequest("chatAgentRequest.json", request -> {
                        request.setConversationId(conversationId);
                        request.setMessage("Invalid payload " + messageNumber);
                    })
                    .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                    .assertDefault();
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().isEmpty()) {
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Unexpected interruption", exception);
                }
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size()).isEqualTo(baselineRuleCount);
        verify(this.openAiChatClient).execute(argThat(request -> Objects.nonNull(request)
                && Objects.equals(request.instruction(), analyzerInstruction)
                && Objects.nonNull(request.input())
                && request.input().contains("Latest user message")));
    }

    @Test
    @DisplayName("Should keep chat success and persist no suggestions when system analyzer is missing")
    void givenAnalyzerMissingInDatabase_whenChatAgentAndPolicyReached_thenKeepChatSuccessAndDoNotPersistSuggestion() {
        //given
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
                .thenReturn("Chat reply");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("No analyzer 1"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int index = 2; index <= 10; index++) {
            final int messageNumber = index;
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.chatAgent())
                    .withPathParameters(PathParams.create().add("agentId", userAgentId))
                    .withRequest("chatAgentRequest.json", request -> {
                        request.setConversationId(conversationId);
                        request.setMessage("No analyzer " + messageNumber);
                    })
                    .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                    .assertDefault();
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().isEmpty()) {
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Unexpected interruption", exception);
                }
            }
        }

        //then
        assertThat(this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size()).isEqualTo(baselineRuleCount);
        verify(this.openAiChatClient, times(10)).execute(any(OpenAiChatRequest.class));
    }

    @Test
    @DisplayName("Should not persist duplicate suggestion when analyzer returns existing rule content")
    void givenAnalyzerSuggestsExistingRuleContent_whenChatAgent_thenDoNotPersistDuplicatePendingRule() {
        //given
        final String analyzerInstruction = "You analyze agent conversations and suggest rules. Return only valid JSON with suggestions.";
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
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setTitle("Existing title");
                    request.setContent("Always answer in Ukrainian unless explicitly asked otherwise.");
                }));

        when(this.openAiChatClient.execute(any(OpenAiChatRequest.class)))
                .thenAnswer(invocation -> {
                    final String instruction = invocation.getArgument(0, OpenAiChatRequest.class).instruction();
                    if (Objects.equals(instruction, analyzerInstruction)) {
                        return """
                                {"suggestions":[{"title":"Duplicate title","content":"Always answer in Ukrainian unless explicitly asked otherwise.","reason":"Duplicate"}]}
                                """;
                    }
                    return "Chat reply";
                });

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", userAgentId))
                .withRequest("chatAgentRequest.json", request -> request.setMessage("Duplicate case 1"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                .assertDefault();
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .max(java.util.Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"))
                .getConversationId();

        for (int index = 2; index <= 10; index++) {
            final int messageNumber = index;
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.chatAgent())
                    .withPathParameters(PathParams.create().add("agentId", userAgentId))
                    .withRequest("chatAgentRequest.json", request -> {
                        request.setConversationId(conversationId);
                        request.setMessage("Duplicate case " + messageNumber);
                    })
                    .andExpectPath(MockMvcResultMatchers.jsonPath("$.reply.content").value("Chat reply"))
                    .assertDefault();
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            if (this.testManager.postgresql().get(AgentRuleEntity.class).getAll().size() == 1) {
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
        final AgentRuleEntity existingRule = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getContent(), "Always answer in Ukrainian unless explicitly asked otherwise."))
                .max(java.util.Comparator.comparing(AgentRuleEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Existing rule not found"));
        assertThat(existingRule.getStatus().getId()).isEqualTo(1L);
        assertThat(existingRule.getAuthorType().getId()).isEqualTo(1L);
    }
}
