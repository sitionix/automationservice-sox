package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationMessageEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationParticipantEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ProjectConversationFlowIT {

    @Autowired
    private TestManager testManager;

    @Test
    @DisplayName("given one attached agent when create project conversation then persist direct conversation and participants")
    void givenOneAttachedAgent_whenCreateProjectConversation_thenPersistDirectConversationAndParticipants() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.type").value("DIRECT"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));

        //then
        final ConversationEntity createdConversation = this.findLatestConversationForProject(projectId);
        assertThat(createdConversation.getType()).isEqualTo(ConversationType.DIRECT);
        assertThat(createdConversation.getStatus()).isEqualTo(ConversationStatus.ACTIVE);
        assertThat(createdConversation.getProjectId()).isEqualTo(projectId);

        final List<ConversationParticipantEntity> participants = this.findParticipants(createdConversation.getConversationId());
        assertThat(participants).hasSize(2);
        assertThat(participants.stream().filter(p -> p.getParticipantType() == ConversationParticipantType.USER).count()).isEqualTo(1);
        assertThat(participants.stream().filter(p -> p.getParticipantType() == ConversationParticipantType.AGENT).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("given multiple attached agents when create project conversation then persist multi agent conversation")
    void givenMultipleAttachedAgents_whenCreateProjectConversation_thenPersistMultiAgentConversation() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        final UUID secondAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");
        this.attachAgent(projectId, secondAgentId, "1");

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.type").value("MULTI_AGENT"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId, secondAgentId))));

        //then
        final ConversationEntity createdConversation = this.findLatestConversationForProject(projectId);
        assertThat(createdConversation.getType()).isEqualTo(ConversationType.MULTI_AGENT);
        assertThat(this.findParticipants(createdConversation.getConversationId())).hasSize(3);
    }

    @Test
    @DisplayName("given empty agent ids when create project conversation then return bad request")
    void givenEmptyAgentIds_whenCreateProjectConversation_thenReturnBadRequest() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .expectStatus(HttpStatus.BAD_REQUEST)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of())));
    }

    @Test
    @DisplayName("given not attached agent when create project conversation then return not found")
    void givenNotAttachedAgent_whenCreateProjectConversation_thenReturnNotFound() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));
    }

    @Test
    @DisplayName("given deleted project when create project conversation then return not found")
    void givenDeletedProject_whenCreateProjectConversation_thenReturnNotFound() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));
    }

    @Test
    @DisplayName("given deleted agent when create project conversation then return not found")
    void givenDeletedAgent_whenCreateProjectConversation_thenReturnNotFound() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgent())
                .withPathParameters(PathParams.create().add("agentId", firstAgentId))
                .assertDefault();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));
    }

    @Test
    @DisplayName("given deleted conversation when list project conversations then exclude deleted conversation")
    void givenDeletedConversation_whenListProjectConversations_thenExcludeDeletedConversation() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));

        final UUID conversationId = this.findLatestConversationForProject(projectId).getConversationId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.deleteAgentConversation())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .assertDefault();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.listProjectConversations())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.items.length()").value(0))
                .assertDefault();
    }

    @Test
    @DisplayName("given existing project conversation when get by project and conversation id then return details")
    void givenExistingProjectConversation_whenGetProjectConversation_thenReturnDetails() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));

        final UUID conversationId = this.findLatestConversationForProject(projectId).getConversationId();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getProjectConversation())
                .withPathParameters(PathParams.create()
                        .add("projectId", projectId)
                        .add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.id").value(conversationId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.projectId").value(projectId.toString()))
                .assertDefault();
    }

    @Test
    @DisplayName("given existing project conversation when submit execution then persist user message and show it in details")
    void givenExistingProjectConversation_whenSubmitExecution_thenPersistUserMessageAndShowItInDetails() {
        //given
        final UUID projectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(projectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));

        final UUID conversationId = this.findLatestConversationForProject(projectId).getConversationId();
        final String message = "Conversation submit message";

        //when
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.submitConversationExecution())
                .withPathParameters(PathParams.create().add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.conversationId").value(conversationId.toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.executionStatus").value("DISPATCH_SKIPPED"))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setMessage(message)));

        //then
        final ChatExecutionEntity persistedExecution = this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversationId(), conversationId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"));
        assertThat(persistedExecution.getStatus().getDescription()).isEqualTo("DISPATCH_SKIPPED");
        assertThat(persistedExecution.getRequestMessage()).isEqualTo(message);

        final ConversationMessageEntity persistedUserMessage = this.testManager.postgresql()
                .get(ConversationMessageEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .filter(entity -> Objects.equals(entity.getMessageId(), persistedExecution.getInputMessageId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Conversation user message not found"));
        assertThat(persistedUserMessage.getContent()).isEqualTo(message);
        assertThat(persistedUserMessage.getAuthorType().name()).isEqualTo("USER");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getProjectConversation())
                .withPathParameters(PathParams.create()
                        .add("projectId", projectId)
                        .add("conversationId", conversationId))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages.length()").value(1))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages[0].id").value(persistedExecution.getInputMessageId().toString()))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages[0].authorType").value("USER"))
                .andExpectPath(MockMvcResultMatchers.jsonPath("$.messages[0].content").value(message))
                .assertDefault();
    }

    @Test
    @DisplayName("given project mismatch when get project conversation then return not found")
    void givenProjectMismatch_whenGetProjectConversation_thenReturnNotFound() {
        //given
        final UUID firstProjectId = this.createProjectForUser("1");
        final UUID secondProjectId = this.createProjectForUser("1");
        final UUID firstAgentId = this.createAndActivateAgent("1");
        this.attachAgent(firstProjectId, firstAgentId, "1");

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", firstProjectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(firstAgentId))));

        final UUID conversationId = this.findLatestConversationForProject(firstProjectId).getConversationId();

        //when then
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.getProjectConversation())
                .withPathParameters(PathParams.create()
                        .add("projectId", secondProjectId)
                        .add("conversationId", conversationId))
                .expectStatus(HttpStatus.NOT_FOUND)
                .assertDefault();
    }

    private UUID createProjectForUser(final String userSub) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject(userSub))
                .assertDefault();

        return this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getOwnerUserId(), Long.valueOf(userSub)))
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
    }

    private UUID createAndActivateAgent(final String userSub) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgent())
                .header("X-Forge-User-Sub", userSub)
                .assertDefault();

        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getUserId(), Long.valueOf(userSub)))
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .header("X-Forge-User-Sub", userSub)
                .assertDefault();

        return agentId;
    }

    private void attachAgent(final UUID projectId, final UUID agentId, final String userSub) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .header("X-Forge-User-Sub", userSub)
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
    }

    private ConversationEntity findLatestConversationForProject(final UUID projectId) {
        return this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getProjectId(), projectId))
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Conversation not found"));
    }

    private List<ConversationParticipantEntity> findParticipants(final UUID conversationId) {
        return this.testManager.postgresql()
                .get(ConversationParticipantEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getConversation().getConversationId(), conversationId))
                .toList();
    }
}
