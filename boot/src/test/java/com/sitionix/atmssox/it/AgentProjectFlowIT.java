package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.QueryParams;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@IntegrationTest
class AgentProjectFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("given valid request when create agent project then return created and persist active project")
    void givenValidRequest_whenCreateAgentProject_thenReturnCreatedAndPersistActiveProject() {
        //given
        final Long ownerUserId = 1L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Marketing Automation"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Project for marketing agents and campaign automation"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getOwnerUserId(), ownerUserId))
                .where(entity -> Objects.equals(entity.getName(), "Marketing Automation"))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Project for marketing agents and campaign automation"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectStatus.ACTIVE.getId()))
                .andExpected(entity -> Objects.nonNull(entity.getProjectId()))
                .andExpected(entity -> Objects.nonNull(entity.getCreatedAt()))
                .andExpected(entity -> Objects.nonNull(entity.getUpdatedAt()))
                .assertEntity();
    }

    @Test
    @DisplayName("given blank description when create agent project then persist null description")
    void givenBlankDescription_whenCreateAgentProject_thenPersistNullDescription() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("17"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").doesNotExist())
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setDescription("   "));
                });

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getOwnerUserId(), 17L))
                .where(entity -> Objects.equals(entity.getName(), "Marketing Automation"))
                .singleElement()
                .andExpected(entity -> Objects.isNull(entity.getDescription()))
                .assertEntity();
    }

    @Test
    @DisplayName("given blank name when create agent project then return bad request and persist nothing")
    void givenBlankName_whenCreateAgentProject_thenReturnBadRequestAndPersistNothing() {
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("99991"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("   ")));

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("given missing user context when create agent project then return unauthorized")
    void givenMissingUserContext_whenCreateAgentProject_thenReturnUnauthorized() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject(null))
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("given two created projects when list agent projects then return projects sorted by updated desc")
    void givenTwoCreatedProjects_whenGetAgentProjects_thenReturnProjectsSortedByUpdatedDesc() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Project A")));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Project B")));

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Project B"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[1].name").value("Project A"))
                .assertDefault();
    }

    @Test
    @DisplayName("given page and size params when list agent projects then return paginated response")
    void givenPageAndSizeQueryParams_whenGetAgentProjects_thenReturnPaginatedProjects() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("42"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Project A"));
                });
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("42"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Project B"));
                });
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("42"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Project C"));
                });

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("42"))
                .withQueryParameters(QueryParams.create().add("page", "1").add("size", "2"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.page").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.size").value(2))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.hasNext").value(false))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].name").value("Project A"))
                .assertDefault();
    }

    @Test
    @DisplayName("given no projects for current user when get agent projects then return empty items")
    void givenNoProjectsForCurrentUser_whenGetAgentProjects_thenReturnEmptyItems() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("2"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Other user project"));
                });

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("777777"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("given missing user context when get agent projects then return unauthorized")
    void givenMissingUserContext_whenGetAgentProjects_thenReturnUnauthorized() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects(null))
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("given negative page when get agent projects then return bad request")
    void givenNegativePage_whenGetAgentProjects_thenReturnBadRequest() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .withQueryParameters(QueryParams.create().add("page", "-1"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("given oversized size when get agent projects then return bad request")
    void givenSizeGreaterThanHundred_whenGetAgentProjects_thenReturnBadRequest() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .withQueryParameters(QueryParams.create().add("size", "101"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("given non numeric page when get agent projects then return bad request")
    void givenNonNumericPage_whenGetAgentProjects_thenReturnBadRequest() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .withQueryParameters(QueryParams.create().add("page", "abc"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("given existing projects when get agent projects repeatedly then keep db unchanged")
    void givenExistingProjects_whenGetAgentProjectsRepeatedly_thenReturnConsistentAndNoDbWrites() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("88"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Read only one"));
                });
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("88"))
                .assertDefault(defaults -> {
                    defaults.mutateRequest(request -> request.setName("Read only two"));
                });
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("88"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .assertDefault();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("88"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("88"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .assertDefault();
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getOwnerUserId(), 88L))
                .hasSize(2)
                .andExpected(entity -> Objects.equals(entity.getOwnerUserId(), 88L))
                .allMatch();
    }
}
