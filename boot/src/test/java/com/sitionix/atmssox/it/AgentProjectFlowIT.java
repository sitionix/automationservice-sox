package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.DatabaseContract;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import com.sitionix.forgeit.mockmvc.api.QueryParams;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
                .where(entity -> Objects.equals(entity.getOwnerUserId(), 99991L))
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

    @Test
    @DisplayName("given active project owned by user when get by id then return project details")
    void givenOwnedActiveProject_whenGetAgentProject_thenReturnProjectDetails() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(projectId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Project Details"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.context").value("Initial project context"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty())
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.updatedAt").isNotEmpty())
                .assertDefault();
    }

    @Test
    @DisplayName("given project belongs to another user when get by id then return not found")
    void givenAnotherUserProject_whenGetAgentProject_thenReturnNotFound() {
        //given
        final UUID projectId = UUID.fromString("f1f77ea9-0822-4d2b-a853-e7c4fa5f631f");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectForeignUserActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "2002")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("given missing user context when get project by id then return unauthorized")
    void givenMissingUserContext_whenGetAgentProject_thenReturnUnauthorized() {
        //given
        final UUID projectId = UUID.fromString("eb8f6c2c-2fcf-4515-9275-2be8db8784a6");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectUnauthorizedContextActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("given owned active project when patch project name then return updated project and persist change")
    void givenOwnedActiveProject_whenPatchAgentProjectName_thenReturnUpdatedAndPersistedProject() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(projectId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Updated Project Name"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .assertDefault();

        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getProjectId(), projectId))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getName(), "Updated Project Name"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectStatus.ACTIVE.getId()))
                .assertEntity();
    }

    @Test
    @DisplayName("given owned active project when patch project description then return updated project and persist change")
    void givenOwnedActiveProject_whenPatchAgentProjectDescription_thenReturnUpdatedAndPersistedProject() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();
        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .applyDefault(defaults -> defaults.withRequest("patchAgentProjectDescriptionOnlyRequest.json"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(projectId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.name").value("Project Details"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.description").value("Updated project description"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getProjectId(), projectId))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getName(), "Project Details"))
                .andExpected(entity -> Objects.equals(entity.getDescription(), "Updated project description"))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectStatus.ACTIVE.getId()))
                .assertEntity();
    }

    @Test
    @DisplayName("given owned active project when patch project context then trim and persist context")
    void givenOwnedActiveProject_whenPatchAgentProjectContext_thenTrimAndPersistContext() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .applyDefault(defaults -> defaults.withRequest("patchAgentProjectContextOnlyRequest.json"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(projectId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.context").value("Updated project context"))
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getProjectId(), projectId))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getContext(), "Updated project context"))
                .assertEntity();
    }

    @Test
    @DisplayName("given blank name when patch project then return bad request")
    void givenBlankName_whenPatchAgentProject_thenReturnBadRequest() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("   ")));
    }

    @Test
    @DisplayName("given unsupported field when patch project then return bad request and keep entity unchanged")
    void givenUnsupportedField_whenPatchAgentProject_thenReturnBadRequestAndKeepEntityUnchanged() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .applyDefault(defaults -> defaults
                        .withRequest("patchAgentProjectUnsupportedFieldRequest.json")
                        .expectStatus(HttpStatus.BAD_REQUEST.value()));

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getProjectId(), projectId))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectStatus.ACTIVE.getId()))
                .andExpected(entity -> Objects.equals(entity.getName(), "Project Details"))
                .assertEntity();
    }

    @Test
    @DisplayName("given foreign project when patch project then return not found")
    void givenAnotherUserProject_whenPatchAgentProject_thenReturnNotFound() {
        //given
        final UUID projectId = UUID.fromString("f1f77ea9-0822-4d2b-a853-e7c4fa5f631f");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectForeignUserActive.json"))
                .build();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "2002")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    @Test
    @DisplayName("given owned active project when delete project then set deleted and hide from read endpoints")
    void givenOwnedActiveProject_whenDeleteAgentProject_thenSoftDeleteAndHideProject() {
        //given
        final UUID projectId = UUID.fromString("b5417721-b65d-4bdd-84bc-80491816f854");
        this.testManager.postgresql()
                .create()
                .to(DatabaseContract.AGENT_PROJECT_ENTITY_DB_CONTRACT.withJson("agentProjectOwnedActive.json"))
                .build();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .assertDefault();

        //then
        this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getProjectId(), projectId))
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectStatus.DELETED.getId()))
                .assertEntity();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1001")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getAgentProjects("1001"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("given owner project and owner agent when add list and remove project agent then membership lifecycle is consistent")
    void givenOwnedProjectAndAgent_whenAddListAndRemoveAgentFromProject_thenPersistAndHideMembership() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID projectId = this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .get(0)
                .getProjectId();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .get(0)
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listAgentProjectAgents())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items[0].id").value(agentId.toString()))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.removeAgentFromProject())
                .withPathParameters(PathParams.create().add("projectId", projectId).add("agentId", agentId))
                .assertDefault();

        //then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listAgentProjectAgents())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
        this.testManager.postgresql()
                .get(AgentProjectMemberEntity.class)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getProject().getProjectId(), projectId))
                .andExpected(entity -> Objects.equals(entity.getAgent().getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectMemberStatus.DELETED.getId()))
                .assertEntity();
    }

    @Test
    @DisplayName("given duplicate add request when add agent to project then keep single active membership")
    void givenDuplicateAddRequest_whenAddAgentToProject_thenKeepSingleMembership() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().get(0).getProjectId();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().get(0).getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));

        //then
        this.testManager.postgresql()
                .get(AgentProjectMemberEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getProject().getProjectId(), projectId))
                .andExpected(entity -> Objects.equals(entity.getAgent().getAgentId(), agentId))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectMemberStatus.ACTIVE.getId()))
                .assertEntity();
    }

    @Test
    @DisplayName("given request without agent id when add agent to project then return bad request and persist nothing")
    void givenMissingAgentId_whenAddAgentToProject_thenReturnBadRequestAndPersistNothing() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().get(0).getProjectId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(null)));

        //then
        this.testManager.postgresql()
                .get(AgentProjectMemberEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("given missing user context when list add and remove project agent then return unauthorized")
    void givenMissingUserContext_whenListAddAndRemoveProjectAgent_thenReturnUnauthorized() {
        //given
        final UUID projectId = UUID.randomUUID();
        final UUID agentId = UUID.randomUUID();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listAgentProjectAgents())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.removeAgentFromProject())
                .withPathParameters(PathParams.create().add("projectId", projectId).add("agentId", agentId))
                .header("X-Forge-User-Sub", null)
                .expectStatus(HttpStatus.UNAUTHORIZED)
                .assertDefault();
    }

    @Test
    @DisplayName("given malformed project id when list and add project agents then return bad request")
    void givenMalformedProjectId_whenListAndAddProjectAgents_thenReturnBadRequest() {
        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listAgentProjectAgents())
                .withPathParameters(PathParams.create().add("projectId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("given malformed agent id when remove agent from project then return bad request")
    void givenMalformedAgentId_whenRemoveAgentFromProject_thenReturnBadRequest() {
        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.removeAgentFromProject())
                .withPathParameters(PathParams.create().add("projectId", UUID.randomUUID()).add("agentId", "%%%"))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault();
    }

    @Test
    @DisplayName("given empty project when list project agents then return empty list")
    void givenEmptyProject_whenListAgentProjectAgents_thenReturnEmptyList() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().get(0).getProjectId();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listAgentProjectAgents())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("given foreign user agent when add agent to project then return not found and keep membership table unchanged")
    void givenForeignUserAgent_whenAddAgentToProject_thenReturnNotFoundAndPersistNothing() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject("1"))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", "2")
                .assertDefault();
        final UUID projectId = this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .where(entity -> Objects.equals(entity.getOwnerUserId(), 1L))
                .getAll()
                .get(0)
                .getProjectId();
        final UUID foreignAgentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .where(entity -> Objects.equals(entity.getUserId(), 2L))
                .getAll()
                .get(0)
                .getAgentId();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", "1")
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(foreignAgentId)));

        //then
        this.testManager.postgresql()
                .get(AgentProjectMemberEntity.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("given no active membership when remove agent from project then return not found and keep active membership untouched")
    void givenNoActiveMembership_whenRemoveAgentFromProject_thenReturnNotFoundAndKeepCurrentState() {
        //given
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .assertDefault();
        final UUID projectId = this.testManager.postgresql().get(AgentProjectEntity.class).getAll().get(0).getProjectId();
        final UUID agentId = this.testManager.postgresql().get(AgentEntity.class).getAll().get(0).getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.removeAgentFromProject())
                .withPathParameters(PathParams.create().add("projectId", projectId).add("agentId", agentId))
                .assertDefault();

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.removeAgentFromProject())
                .withPathParameters(PathParams.create().add("projectId", projectId).add("agentId", agentId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();

        //then
        final List<AgentProjectMemberEntity> memberships = this.testManager.postgresql().get(AgentProjectMemberEntity.class).getAll();
        this.testManager.postgresql()
                .get(AgentProjectMemberEntity.class)
                .hasSize(1)
                .singleElement()
                .andExpected(entity -> Objects.equals(entity.getMembershipId(), memberships.get(0).getMembershipId()))
                .andExpected(entity -> Objects.equals(entity.getStatus().getId(), AgentProjectMemberStatus.DELETED.getId()))
                .assertEntity();
    }
}
