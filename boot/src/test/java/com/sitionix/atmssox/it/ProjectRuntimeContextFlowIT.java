package com.sitionix.atmssox.it;

import com.sitionix.atmssox.application.usecase.ChatExecutionAsyncProcessor;
import com.sitionix.atmssox.application.usecase.ChatExecutionAsyncRunner;
import com.sitionix.atmssox.domain.client.OpenAiChatClient;
import com.sitionix.atmssox.domain.client.OpenAiChatRequest;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@IntegrationTest
class ProjectRuntimeContextFlowIT {

    @Autowired
    private TestManager testManager;

    @Autowired
    private ChatExecutionAsyncProcessor chatExecutionAsyncProcessor;

    @MockBean
    private ChatExecutionAsyncRunner chatExecutionAsyncRunner;

    @MockBean
    private OpenAiChatClient openAiChatClient;

    @Test
    @DisplayName("Should inject project context block before messages for project-bound conversations")
    void givenProjectBoundConversation_whenProcessExecution_thenProjectContextIsPrepended() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        final UUID agentId = this.createActiveAgent();
        final UUID projectId = this.createProject("Alpha Project", true);
        this.attachAgent(projectId, agentId);
        final UUID conversationId = this.createProjectConversation(projectId, agentId);
        final UUID executionId = this.submitExecution(agentId, conversationId, "Continue with project details");

        //when
        this.chatExecutionAsyncProcessor.process(executionId);

        //then
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        final String runtimeInput = requestCaptor.getValue().input();
        assertThat(runtimeInput).contains("Updated project context");
        assertThat(runtimeInput).contains("Messages:");
        assertThat(runtimeInput.indexOf("Updated project context")).isLessThan(runtimeInput.indexOf("Messages:"));
    }

    @Test
    @DisplayName("Should not inject project context block for standalone conversations")
    void givenStandaloneConversation_whenProcessExecution_thenProjectContextIsAbsent() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        final UUID agentId = this.createActiveAgent();
        final UUID executionId = this.submitExecution(agentId, null, "Standalone request");

        //when
        this.chatExecutionAsyncProcessor.process(executionId);

        //then
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        final String runtimeInput = requestCaptor.getValue().input();
        assertThat(runtimeInput).doesNotContain("Updated project context");
        assertThat(runtimeInput).contains("Messages:");
    }

    @Test
    @DisplayName("Should render stable fallback for blank project context")
    void givenProjectWithBlankContext_whenProcessExecution_thenFallbackProjectContextIsUsed() {
        //given
        when(this.openAiChatClient.execute(any())).thenReturn("reply");
        final UUID agentId = this.createActiveAgent();
        final UUID projectId = this.createProject("Fallback Project", false);
        this.attachAgent(projectId, agentId);
        final UUID conversationId = this.createProjectConversation(projectId, agentId);
        final UUID executionId = this.submitExecution(agentId, conversationId, "Need fallback rendering");

        //when
        this.chatExecutionAsyncProcessor.process(executionId);

        //then
        final ArgumentCaptor<OpenAiChatRequest> requestCaptor = ArgumentCaptor.forClass(OpenAiChatRequest.class);
        verify(this.openAiChatClient).execute(requestCaptor.capture());
        final String runtimeInput = requestCaptor.getValue().input();
        assertThat(runtimeInput).contains("No additional project context provided.");
    }

    private UUID createActiveAgent() {
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
        return agentId;
    }

    private UUID createProject(final String name, final boolean withContextPatch) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createAgentProject())
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setName(name)));
        final UUID projectId = this.testManager.postgresql()
                .get(AgentProjectEntity.class)
                .getAll()
                .stream()
                .max(Comparator.comparing(AgentProjectEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project not found"))
                .getProjectId();

        if (withContextPatch) {
            this.testManager.mockMvc()
                    .ping(ControllerEndpoint.patchAgentProject())
                    .withPathParameters(PathParams.create().add("projectId", projectId))
                    .applyDefault(defaults -> defaults.withRequest("patchAgentProjectContextOnlyRequest.json"))
                    .assertDefault();
        }
        return projectId;
    }

    private void attachAgent(final UUID projectId, final UUID agentId) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.addAgentToProject())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentId(agentId)));
    }

    private UUID createProjectConversation(final UUID projectId, final UUID agentId) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.createProjectConversation())
                .withPathParameters(PathParams.create().add("projectId", projectId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> request.setAgentIds(Set.of(agentId))));

        return this.testManager.postgresql()
                .get(ConversationEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getProjectId(), projectId))
                .max(Comparator.comparing(ConversationEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Project conversation not found"))
                .getConversationId();
    }

    private UUID submitExecution(final UUID agentId, final UUID conversationId, final String message) {
        this.testManager.mockMvc()
                .ping(ControllerEndpoint.chatAgent())
                .withPathParameters(PathParams.create().add("agentId", agentId))
                .assertDefault(defaults -> defaults.mutateRequest(request -> {
                    request.setConversationId(conversationId);
                    request.setMessage(message);
                }));

        return this.testManager.postgresql()
                .get(ChatExecutionEntity.class)
                .getAll()
                .stream()
                .filter(entity -> Objects.equals(entity.getAgentId(), agentId))
                .max(Comparator.comparing(ChatExecutionEntity::getCreatedAt))
                .orElseThrow(() -> new AssertionError("Execution not found"))
                .getExecutionId();
    }
}
