package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@IntegrationTest
class GetAgentAndGetAgentsFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should return only current user agents ordered by updatedAt desc")
    void givenAgentsForMultipleUsers_whenGetAgents_thenReturnOnlyCurrentUserAgentsInUpdatedAtDescOrder() {
        // given
        this.createAgentForUser("1", "First user one", "Description one");
        final UUID latestUserOneAgentId = this.createAgentForUser("1", "Second user one", "Description two");
        this.createAgentForUser("2", "Other user", "Description three");

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(latestUserOneAgentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Second user one"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[1].name").value("First user one"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return agent by id for current user")
    void givenExistingAgentOfCurrentUser_whenGetAgent_thenReturnAgent() {
        // given
        final UUID agentId = this.createAgentForUser("1", "Single fetch", "Single fetch description");

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(agentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Single fetch"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Single fetch description"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("DRAFT"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return unauthorized when user context is missing for get agents")
    void givenMissingUserContext_whenGetAgents_thenReturnUnauthorized() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return forbidden when S2S token is missing for get agents")
    void givenMissingS2sToken_whenGetAgents_thenReturnForbidden() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .token(null)
                .expectStatus(HttpStatus.FORBIDDEN)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return empty list when current user has no agents")
    void givenNoAgentsForCurrentUser_whenGetAgents_thenReturnEmptyItems() {
        // given
        this.createAgentForUser("1", "Existing for another user", "Existing description");

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .header("X-Forge-User-Sub", "999")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return not found when agent does not belong to current user")
    void givenMissingAgentForCurrentUser_whenGetAgent_thenReturnNotFound() {
        // given
        final UUID anotherUserAgentId = this.createAgentForUser("2", "Another user agent", "Another user description");

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", anotherUserAgentId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return not found when agent is deleted")
    void givenDeletedAgent_whenGetAgent_thenReturnNotFound() {
        // given
        final UUID agentId = this.createAgentForUser("1", "Deleted single fetch", "Deleted single fetch description");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("Should exclude deleted agents from current user list")
    void givenDeletedAndVisibleAgents_whenGetAgents_thenExcludeDeletedFromList() {
        // given
        final UUID visibleAgentId = this.createAgentForUser("1", "Visible user one", "Visible description");
        final UUID deletedAgentId = this.createAgentForUser("1", "Deleted user one", "Deleted description");
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", deletedAgentId))
                .assertDefault();

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(visibleAgentId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Visible user one"))
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request for invalid agent id format")
    void givenInvalidAgentIdFormat_whenGetAgent_thenReturnBadRequest() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", "123"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should return bad request for malformed agent id path")
    void givenMalformedAgentIdPath_whenGetAgent_thenReturnBadRequest() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgent())
                .withPathParameters(PathParams.create().add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("Should keep DB unchanged on repeated get agents calls")
    void givenExistingAgents_whenGetAgentsRepeatedly_thenReturnConsistentResponseAndNoDbWrites() {
        // given
        final UUID agentId = this.createAgentForUser("1", "Idempotent read", "Idempotent read description");
        final int beforeReadSize = this.testManager.postgresql().get(AgentEntity.class).getAll().size();

        // when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(agentId.toString()))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgents())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(agentId.toString()))
                .assertDefault();

        final int afterReadSize = this.testManager.postgresql().get(AgentEntity.class).getAll().size();
        if (!Objects.equals(beforeReadSize, afterReadSize)) {
            throw new AssertionError("GET requests must not create or modify agents");
        }
    }

    private UUID createAgentForUser(final String userSub, final String name, final String description) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", userSub)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setName(name);
                    request.setDescription(description);
                }));

        return this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getUserId(), Long.valueOf(userSub)))
                .filter(entity -> Objects.equals(entity.getName(), name))
                .filter(entity -> Objects.equals(entity.getDescription(), description))
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .map(AgentEntity::getAgentId)
                .orElseThrow(() -> new AssertionError("Created agent not found in DB"));
    }
}
