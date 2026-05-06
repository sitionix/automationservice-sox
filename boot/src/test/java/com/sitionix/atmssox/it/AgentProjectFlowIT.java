package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.QueryParams;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class AgentProjectFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("given valid request when create agent project then return created and persist active project")
    void givenValidRequest_whenCreateAgentProject_thenReturnCreatedAndPersistActiveProject() {
        //given
        final Long userId = 1L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();

        //then
        final AgentProjectEntity project = this.getLatestProjectByOwner(userId);
        assertThat(project.getOwnerUserId()).isEqualTo(userId);
        assertThat(project.getName()).isEqualTo("Marketing Automation");
        assertThat(project.getDescription()).isEqualTo("Project for marketing agents and campaign automation");
        assertThat(project.getStatus()).isEqualTo(AgentProjectStatus.ACTIVE);
        assertThat(project.getProjectId()).isNotNull();
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("given blank description when create agent project then persist null description")
    void givenBlankDescription_whenCreateAgentProject_thenPersistNullDescription() {
        //given
        final Long userId = 17L;

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", String.valueOf(userId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setDescription("   ")));

        //then
        final AgentProjectEntity project = this.getLatestProjectByOwner(userId);
        assertThat(project.getOwnerUserId()).isEqualTo(userId);
        assertThat(project.getName()).isEqualTo("Marketing Automation");
        assertThat(project.getDescription()).isNull();
    }

    @Test
    @DisplayName("given blank name when create agent project then return bad request and persist nothing")
    void givenBlankName_whenCreateAgentProject_thenReturnBadRequestAndPersistNothing() {
        //given
        final int projectsBefore = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().size();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("   ")));

        //then
        final int projectsAfter = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().size();
        assertThat(projectsAfter).isEqualTo(projectsBefore);
    }

    @Test
    @DisplayName("given missing user context when create agent project then return unauthorized")
    void givenMissingUserContext_whenCreateAgentProject_thenReturnUnauthorized() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", null)
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
        final String userSub = "42";
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Project A")));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Project B")));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Project C")));

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .header("X-Forge-User-Sub", userSub)
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
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", "2")
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Other user project")));

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .header("X-Forge-User-Sub", "777777")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("given missing user context when get agent projects then return unauthorized")
    void givenMissingUserContext_whenGetAgentProjects_thenReturnUnauthorized() {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .header("X-Forge-User-Sub", null)
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
        final String userSub = "88";
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Read only one")));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Read only two")));

        final int beforeReadSize = (int) this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .filter(project -> Objects.equals(project.getOwnerUserId(), Long.valueOf(userSub)))
                .count();

        //when/then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .header("X-Forge-User-Sub", userSub)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects())
                .header("X-Forge-User-Sub", userSub)
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(2))
                .assertDefault();

        final int afterReadSize = (int) this.testManager.postgresql().get(AgentProjectEntity.class).getAll().stream()
                .filter(project -> Objects.equals(project.getOwnerUserId(), Long.valueOf(userSub)))
                .count();
        if (!Objects.equals(beforeReadSize, afterReadSize)) {
            throw new AssertionError("GET requests must not create or modify agent projects");
        }
    }

    private AgentProjectEntity getLatestProjectByOwner(final Long userId) {
        return this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .stream()
                .filter(project -> Objects.equals(project.getOwnerUserId(), userId))
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent project not found for userId=" + userId));
    }
}
