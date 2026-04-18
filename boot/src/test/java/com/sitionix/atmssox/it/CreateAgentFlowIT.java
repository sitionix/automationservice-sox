package com.sitionix.atmssox.it;

import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CreateAgentFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("Should create agent and persist it in PostgreSQL")
    void givenValidRequest_whenCreateAgent_thenReturnCreatedAndPersistAgent() {
        //given
        final Long userId = 1L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .expectResponse("createAgentResponse.json", "id", "createdAt", "updatedAt")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getUserId(), userId))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .andExpected(entity -> Objects.nonNull(entity.getAgentId()))
                .andExpected(entity -> Objects.nonNull(entity.getCreatedAt()))
                .andExpected(entity -> Objects.nonNull(entity.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should trim name and description before persisting")
    void givenPaddedFields_whenCreateAgent_thenPersistTrimmedValues() {
        //given
        final Long userId = 1L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Architecture Reviewer"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Minimal internal agent foundation entry"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setName("   Architecture Reviewer   ");
                    request.setDescription("   Minimal internal agent foundation entry   ");
                }));

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getUserId(), userId))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Minimal internal agent foundation entry"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .assertEntity();
    }

    @Test
    @DisplayName("Should return unauthorized and persist nothing when user context is missing")
    void givenMissingUserContext_whenCreateAgent_thenReturnUnauthorizedAndPersistNothing() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return forbidden and persist nothing when S2S token is missing")
    void givenMissingS2sToken_whenCreateAgent_thenReturnForbiddenAndPersistNothing() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .token(null)
                .expectStatus(HttpStatus.FORBIDDEN)
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should return bad request and persist nothing for blank name")
    void givenBlankName_whenCreateAgent_thenReturnBadRequestAndPersistNothing() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setName("   ")));

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("Should create agent with null description")
    void givenMissingDescription_whenCreateAgent_thenPersistNullDescription() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .expectStatus(HttpStatus.CREATED)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setDescription(null)));

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .assertEntity();
    }

    @Test
    @DisplayName("Should allow duplicate agent names")
    void givenDuplicateNameRequests_whenCreateAgentTwice_thenPersistBothAgents() {
        //given
        final Long userId = 1L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(2)
                .andExpected(entity -> Objects.equals(entity.getUserId(), userId))
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .allMatch();

        final List<AgentEntity> agents = this.testManager.postgresql().get(AgentEntity.class).getAll();
        assertThat(agents)
                .extracting(AgentEntity::getAgentId)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Should keep single persisted row when second request fails validation")
    void givenSecondRequestInvalid_whenCreateAgentTwice_thenRollbackSecondAndKeepFirst() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults
                        .mutateRequest(request -> request.setDescription("   ")));

        //then
        this.testManager.postgresql()
                .get(AgentEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getName(), "Architecture Reviewer"))
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), 1L))
                .assertEntity();
    }
}
