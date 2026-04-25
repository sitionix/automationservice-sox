package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import com.sitionix.forgeit.mockmvc.api.QueryParams;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AgentRuleFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should execute full agent rule lifecycle flow")
    void givenActiveRuleLifecycle_whenCreateGetPatchDeleteThenGet_thenReturnExpectedResponsesAndPersistSoftDelete() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("Deterministic output"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.content").value("Always produce deterministic output"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.authorType").value("USER"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        final AgentRuleEntity createdRule = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(createdRule.getRuleId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].title").value("Deterministic output"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].content").value("Always produce deterministic output"))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("ruleId", createdRule.getRuleId()))
                .withRequest("patchAgentRuleRequest.json")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(createdRule.getRuleId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("Response style"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.content").value("Keep responses concise and technical"))
                .assertDefault();

        final Instant beforeDeleteUpdatedAt = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("ruleId", createdRule.getRuleId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .assertDefault();

        //then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();

        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId(), createdRule.getRuleId()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), "Response style"))
                .andExpected(entity -> Objects.equals(entity.getContent(), "Keep responses concise and technical"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.DELETED.getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType().name(), "USER"))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeDeleteUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return only active rules by default and include status and author type")
    void givenMixedRuleStatuses_whenGetRulesWithoutFilters_thenReturnOnlyActiveRules() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveUser.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleRejectedAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleDeletedAi.json"))
                .build();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].authorType").value("USER"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return only pending AI rules for suggested filter")
    void givenMixedRuleStatuses_whenGetRulesWithPendingAiFilter_thenReturnSuggestedRulesOnly() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveUser.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleRejectedAi.json"))
                .build();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111"))
                .withQueryParameters(QueryParams.create()
                        .add("status", "PENDING")
                        .add("authorType", "AI"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].status").value("PENDING"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].authorType").value("AI"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should accept pending AI rule and activate with edited text")
    void givenPendingAiRule_whenAcceptWithEdits_thenActivateRuleAndUpdateText() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .build();

        final Instant beforeAcceptUpdatedAt = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.acceptAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.authorType").value("AI"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("Accepted rule title"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.content").value("Accepted rule content"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId().toString(), "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.ACTIVE.getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType().name(), "AI"))
                .andExpected(entity -> Objects.equals(entity.getTitle(), "Accepted rule title"))
                .andExpected(entity -> Objects.equals(entity.getContent(), "Accepted rule content"))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeAcceptUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should accept rejected AI rule with missing fields and keep current text")
    void givenRejectedAiRule_whenAcceptWithMissingFields_thenActivateRuleAndKeepText() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleRejectedAi.json"))
                .build();

        final AgentRuleEntity beforeAccept = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.acceptAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .expectStatus(HttpStatus.OK)
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setTitle(null);
                    request.setContent(null);
                }));

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId(), beforeAccept.getRuleId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.ACTIVE.getId()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeAccept.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeAccept.getContent()))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeAccept.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should accept deleted AI rule and activate with edited text")
    void givenDeletedAiRule_whenAcceptWithEdits_thenActivateRuleAndUpdateText() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleDeletedAi.json"))
                .build();

        final Instant beforeAcceptUpdatedAt = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.acceptAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "dddddddd-dddd-dddd-dddd-dddddddddddd"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.authorType").value("AI"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.title").value("Accepted rule title"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.content").value("Accepted rule content"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId().toString(), "dddddddd-dddd-dddd-dddd-dddddddddddd"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.ACTIVE.getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType().name(), "AI"))
                .andExpected(entity -> Objects.equals(entity.getTitle(), "Accepted rule title"))
                .andExpected(entity -> Objects.equals(entity.getContent(), "Accepted rule content"))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeAcceptUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when accepting active AI rule")
    void givenActiveAiRule_whenAccept_thenReturnConflictAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveAi.json"))
                .build();

        final AgentRuleEntity beforeAccept = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.acceptAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "ffffffff-ffff-ffff-ffff-ffffffffffff"))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeAccept.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType(), beforeAccept.getAuthorType()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeAccept.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeAccept.getContent()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeAccept.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should reject pending AI rule")
    void givenPendingAiRule_whenReject_thenSetRejectedStatus() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .build();

        final Instant beforeRejectUpdatedAt = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("REJECTED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.authorType").value("AI"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.REJECTED.getId()))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeRejectUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when rejecting USER rule")
    void givenPendingUserRule_whenReject_thenReturnConflictAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingUser.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType(), beforeReject.getAuthorType()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeReject.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeReject.getContent()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when rejecting non pending AI rule")
    void givenActiveAiRule_whenReject_thenReturnConflictAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleActiveAi.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "ffffffff-ffff-ffff-ffff-ffffffffffff"))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType(), beforeReject.getAuthorType()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeReject.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeReject.getContent()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when rejecting deleted AI rule")
    void givenDeletedAiRule_whenReject_thenReturnConflictAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleDeletedAi.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "dddddddd-dddd-dddd-dddd-dddddddddddd"))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType(), beforeReject.getAuthorType()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeReject.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeReject.getContent()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when rejecting already rejected AI rule")
    void givenRejectedAiRule_whenReject_thenReturnConflictAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRuleRejectedAi.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getAuthorType(), beforeReject.getAuthorType()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforeReject.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforeReject.getContent()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should delete pending AI rule with soft delete status")
    void givenPendingAiRule_whenDelete_thenSetDeletedStatus() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .build();

        final Instant beforeDeleteUpdatedAt = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentRuleStatus.DELETED.getId()))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeDeleteUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return empty rules list for owned agent without rules")
    void givenAgentWithoutRules_whenGetRules_thenReturnEmptyItems() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();

        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return active rules ordered by createdAt and exclude deleted")
    void givenMultipleRules_whenGetRules_thenReturnOnlyActiveRulesInCreatedAtOrder() {
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
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final UUID firstRuleId = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getRuleId();

        try {
            Thread.sleep(5L);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interruption", e);
        }

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setTitle("Second rule");
                    request.setContent("Second rule for ordering");
                }));

        final List<AgentRuleEntity> allRules = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .getAll();
        final UUID secondRuleId = allRules.stream()
                .filter(entity -> Objects.equals(entity.getContent(), "Second rule for ordering"))
                .map(AgentRuleEntity::getRuleId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Second rule not found"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(firstRuleId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].content").value("Always produce deterministic output"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[1].id").value(secondRuleId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[1].content").value("Second rule for ordering"))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("ruleId", firstRuleId))
                .assertDefault();

        //then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentRules())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(secondRuleId.toString()))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request and persist nothing for blank create title")
    void givenBlankRuleText_whenCreateRule_thenReturnBadRequestAndPersistNothing() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .withRequest("createAgentRuleBlankRequest.json")
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return conflict and keep row unchanged when patch has no fields")
    void givenNullPatchBodyText_whenPatchRule_thenReturnBadRequestAndKeepRuleUnchanged() {
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
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final AgentRuleEntity beforePatch = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", agentId)
                        .add("ruleId", beforePatch.getRuleId()))
                .withRequest("patchAgentRuleNullTextRequest.json")
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId(), beforePatch.getRuleId()))
                .andExpected(entity -> Objects.equals(entity.getTitle(), beforePatch.getTitle()))
                .andExpected(entity -> Objects.equals(entity.getContent(), beforePatch.getContent()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforePatch.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforePatch.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and persist nothing when user context is missing for create rule")
    void givenMissingUserContext_whenCreateRule_thenReturnUnauthorizedAndPersistNothing() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return bad request for malformed rule id")
    void givenMalformedRuleId_whenPatchRule_thenReturnBadRequest() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("ruleId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return bad request for malformed rule id on reject")
    void givenMalformedRuleId_whenRejectRule_thenReturnBadRequest() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create().add("agentId", "11111111-1111-1111-1111-111111111111").add("ruleId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId(), beforeReject.getRuleId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and keep row unchanged when user context is missing for reject")
    void givenMissingUserContext_whenRejectRule_thenReturnUnauthorizedAndKeepRowUnchanged() {
        //given
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_ENTITY_DB_CONTRACT.withJson("agentRuleOwnerActiveAgent.json"))
                .to(DatabaseContract.AGENT_RULE_ENTITY_DB_CONTRACT.withJson("agentRulePendingAi.json"))
                .build();

        final AgentRuleEntity beforeReject = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.rejectAgentRule())
                .withPathParameters(PathParams.create()
                        .add("agentId", "11111111-1111-1111-1111-111111111111")
                        .add("ruleId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getRuleId(), beforeReject.getRuleId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), beforeReject.getStatus().getId()))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), beforeReject.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should keep deleted row unchanged on repeated delete")
    void givenAlreadyDeletedRule_whenDeleteRuleTwice_thenReturnDeletedAndKeepSingleDeletedRow() {
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
                .ping(ControllerEndpoint.createAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final UUID ruleId = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity()
                .getRuleId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("ruleId", ruleId))
                .expectStatus(HttpStatus.OK)
                .assertDefault();

        final AgentRuleEntity afterFirstDelete = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentRule())
                .withPathParameters(PathParams.create().add("agentId", agentId).add("ruleId", ruleId))
                .expectStatus(HttpStatus.CONFLICT)
                .assertDefault();

        final AgentRuleEntity afterSecondDelete = this.testManager.postgresql()
                .get(AgentRuleEntity.class)
                .singleElement()
                .assertEntity();

        //then
        assertThat(afterSecondDelete.getStatus().getId()).isEqualTo(AgentRuleStatus.DELETED.getId());
        assertThat(afterSecondDelete.getUpdatedAt()).isEqualTo(afterFirstDelete.getUpdatedAt());
        assertThat(afterSecondDelete.getTitle()).isEqualTo(afterFirstDelete.getTitle());
        assertThat(afterSecondDelete.getContent()).isEqualTo(afterFirstDelete.getContent());
    }
}
