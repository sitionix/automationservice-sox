package com.sitionix.atmssox.it;

import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.it.infra.ControllerEndpoint;
import com.sitionix.atmssox.it.infra.TestManager;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.forgeit.core.test.IntegrationTest;
import com.sitionix.forgeit.mockmvc.api.PathParams;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
class ProjectRuntimeContextFlowIT {

    @Autowired
    private TestManager testManager;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should inject project context block before messages for project-bound conversations")
    void givenProjectBoundConversation_whenProcessExecution_thenProjectContextIsPrepended() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();

        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Alpha Project")));
        final UUID projectId = this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.patchAgentProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .applyDefault(defaults -> defaults.withRequest("patchAgentProjectContextOnlyRequest.json"))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getProjectId(), projectId))
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project conversation not found"))
                .getConversationId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Continue with project details");
                }));
        //when
        //then
        verify(this.openAiChatClient, timeout(4000)).execute(argThat(request -> {
            final String runtimeInput = request.input();
            return runtimeInput.contains("Updated project context")
                    && runtimeInput.contains("Messages:")
                    && runtimeInput.indexOf("Updated project context") < runtimeInput.indexOf("Messages:");
        }));
    }

    @Test
    @DisplayName("Should not inject project context block for standalone conversations")
    void givenStandaloneConversation_whenProcessExecution_thenProjectContextIsAbsent() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setConversationId(null);
                    request.setMessage("Standalone request");
                }));
        //when
        //then
        verify(this.openAiChatClient, timeout(4000)).execute(argThat(request -> {
            final String runtimeInput = request.input();
            return !runtimeInput.contains("Updated project context") && runtimeInput.contains("Messages:");
        }));
    }

    @Test
    @DisplayName("Should render stable fallback for blank project context")
    void givenProjectWithBlankContext_whenProcessExecution_thenFallbackProjectContextIsUsed() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        this.testManager.mockMvc().ping(ControllerEndpoint.createAgent()).assertDefault();
        final UUID agentId = this.testManager.postgresql()
                .get(AgentEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Agent not found"))
                .getAgentId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.activateAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName("Fallback Project")));
        final UUID projectId = this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));
        final UUID conversationId = this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getProjectId(), projectId))
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project conversation not found"))
                .getConversationId();
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setConversationId(conversationId);
                    request.setMessage("Need fallback rendering");
                }));
        //when
        //then
        verify(this.openAiChatClient, timeout(4000)).execute(argThat(request ->
                request.input().contains("No additional project context provided.")
        ));
    }

}
