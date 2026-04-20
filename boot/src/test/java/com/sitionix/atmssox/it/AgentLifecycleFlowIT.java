package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@IntegrationTest
class AgentLifecycleFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should activate draft agent and persist active status")
    void givenDraftAgent_whenActivate_thenReturnOkAndPersistActiveStatus() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity draftAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();
        final UUID agentId = draftAgent.getAgentId();
        final Instant beforeUpdate = draftAgent.getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeUpdate))
                .assertEntity();
    }

    @Test
    @DisplayName("Should archive active agent and persist archived status")
    void givenActiveAgent_whenArchive_thenReturnOkAndPersistArchivedStatus() {
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

        final Instant beforeUpdate = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ARCHIVED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 3L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeUpdate))
                .assertEntity();
    }

    @Test
    @DisplayName("Should archive draft agent and persist archived status")
    void givenDraftAgent_whenArchive_thenReturnOkAndPersistArchivedStatus() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        final AgentEntity draftAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity();

        final Instant beforeUpdate = draftAgent.getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(draftAgent.getAgentId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ARCHIVED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 3L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(beforeUpdate))
                .assertEntity();
    }

    @Test
    @DisplayName("Should restore archived agent and persist draft status")
    void givenArchivedAgent_whenRestore_thenReturnOkAndPersistDraftStatus() {
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
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant archivedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.restoreAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(archivedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should soft delete draft agent and persist deleted status")
    void givenDraftAgent_whenDelete_thenReturnOkAndPersistDeletedStatus() {
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
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(draftAgent.getAgentId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 4L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(draftAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should soft delete active agent and persist deleted status")
    void givenActiveAgent_whenDelete_thenReturnOkAndPersistDeletedStatus() {
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

        final Instant activeUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 4L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(activeUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should soft delete archived agent and persist deleted status")
    void givenArchivedAgent_whenDelete_thenReturnOkAndPersistDeletedStatus() {
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
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant archivedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 4L))
                .andExpected(entity -> entity.getUpdatedAt().isAfter(archivedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep draft status when restoring draft agent")
    void givenDraftAgent_whenRestore_thenReturnConflictAndKeepDraftStatus() {
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
                .ping(ControllerEndpoint.restoreAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .expectStatus(HttpStatus.CONFLICT)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(409))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("Invalid agent transition: DRAFT -> DRAFT"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), draftAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return success and keep deleted status when deleting deleted agent again")
    void givenDeletedAgent_whenDeleteAgain_thenReturnSuccessAndKeepDeletedStatus() {
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
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant deletedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.OK)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DELETED"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 4L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), deletedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should hide deleted agent from get by id and get agents list")
    void givenDeletedAgent_whenGetByIdAndGetAgents_thenHideDeletedButKeepRowInDb() {
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
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 4L))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep active status when activating active agent again")
    void givenAlreadyActiveAgent_whenActivateAgain_thenReturnConflictAndKeepStatus() {
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

        final Instant activeUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.CONFLICT)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(409))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("Invalid agent transition: ACTIVE -> ACTIVE"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict and keep archived status when archiving archived agent again")
    void givenAlreadyArchivedAgent_whenArchiveAgain_thenReturnConflictAndKeepStatus() {
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

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant archivedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.CONFLICT)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(409))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("Invalid agent transition: ARCHIVED -> ARCHIVED"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 3L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), archivedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return conflict when activating archived agent")
    void givenArchivedAgent_whenActivate_thenReturnConflictWithErrorAndNoStatusChange() {
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

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant archivedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.CONFLICT)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(409))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.details").value("Invalid agent transition: ARCHIVED -> ACTIVE"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 3L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), archivedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and keep draft when user context is missing")
    void givenMissingUserContext_whenActivate_thenReturnUnauthorizedAndDoNotChangeAgent() {
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
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), draftAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and keep draft when user context is missing for delete")
    void givenMissingUserContext_whenDelete_thenReturnUnauthorizedAndDoNotChangeAgent() {
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
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", draftAgent.getAgentId()))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), draftAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return forbidden and keep active when S2S token is missing")
    void givenMissingS2sToken_whenArchive_thenReturnForbiddenAndDoNotChangeAgent() {
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

        final Instant activeUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .token(null)
                .expectStatus(HttpStatus.FORBIDDEN)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return forbidden and keep archived when S2S token is missing for restore")
    void givenMissingS2sToken_whenRestore_thenReturnForbiddenAndDoNotChangeAgent() {
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
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        final Instant archivedUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.restoreAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .token(null)
                .expectStatus(HttpStatus.FORBIDDEN)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 3L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), archivedUpdatedAt))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return not found when agent belongs to another user")
    void givenAgentOfAnotherUser_whenActivate_thenReturnNotFoundAndDoNotChangeStatus() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", "2")
                .assertDefault();

        final AgentEntity anotherUserAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getUserId(), 2L))
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Expected user 2 agent to exist"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", anotherUserAgent.getAgentId()))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), anotherUserAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return not found when deleting agent that belongs to another user")
    void givenAgentOfAnotherUser_whenDelete_thenReturnNotFoundAndDoNotChangeStatus() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", "2")
                .assertDefault();

        final AgentEntity anotherUserAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getUserId(), 2L))
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Expected user 2 agent to exist"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", anotherUserAgent.getAgentId()))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), anotherUserAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return not found when archiving agent that belongs to another user")
    void givenAgentOfAnotherUser_whenArchive_thenReturnNotFoundAndDoNotChangeStatus() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", "2")
                .assertDefault();

        final AgentEntity anotherUserAgent = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getUserId(), 2L))
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Expected user 2 agent to exist"));

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.archiveAgent())
                .withPathParameters(PathParams.create().add("agentId", anotherUserAgent.getAgentId()))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), anotherUserAgent.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return bad request for malformed agent id in activate endpoint")
    void givenMalformedAgentId_whenActivate_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return bad request for malformed agent id in restore endpoint")
    void givenMalformedAgentId_whenRestore_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.restoreAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return bad request for malformed agent id in delete endpoint")
    void givenMalformedAgentId_whenDelete_thenReturnBadRequest() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return conflict on activate retry after successful activation")
    void givenAgentActivatedOnce_whenRetryActivate_thenReturnConflict() {
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

        final Instant activeUpdatedAt = this.testManager.postgresql()
                .get(AgentEntity.class)
                .singleElement()
                .assertEntity()
                .getUpdatedAt();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.CONFLICT)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.code").value(409))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 2L))
                .andExpected(entity -> Objects.equals(entity.getUpdatedAt(), activeUpdatedAt))
                .assertEntity();
    }
}
