package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.WorkspaceSitesArg;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkspaceSitesCapabilityHandlerTest {

    private GetWorkspaceSitesCapabilityHandler getWorkspaceSitesCapabilityHandler;

    @Mock
    private WorkspaceProjectionClient workspaceProjectionClient;

    @BeforeEach
    void setUp() {
        this.getWorkspaceSitesCapabilityHandler = new GetWorkspaceSitesCapabilityHandler(this.workspaceProjectionClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenDefinition_whenCalled_thenReturnExpectedCapabilityMetadataAndEmptySchema() {
        //given

        //when
        final CapabilityDefinition actual = this.getWorkspaceSitesCapabilityHandler.definition();

        //then
        assertThat(actual.name()).isEqualTo(CapabilityName.GET_WORKSPACE_SITES.name());
        assertThat(actual.tags()).contains("site", "workspace", "list");
        assertThat(actual.inputSchema().type()).isEqualTo("object");
        assertThat(actual.inputSchema().properties()).isEqualTo(Map.of());
        assertThat(actual.inputSchema().required()).isEqualTo(List.of());
        assertThat(actual.inputSchema().additionalProperties()).isEqualTo(Boolean.FALSE);
    }

    @Test
    void givenValidCommand_whenExecute_thenReturnPayloadAndCallWorkspaceProjectionClient() {
        //given
        final CapabilityExecutionCommand<WorkspaceSitesArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("f4a0ef0f-84df-4b2d-a3dd-3cb13a9356e1"),
                new WorkspaceSitesArg()
        );
        final JsonNode givenPayload = new ObjectMapper().valueToTree(Map.of("total", 1));
        when(this.workspaceProjectionClient.getWorkspaceSites(17L)).thenReturn(givenPayload);

        //when
        final CapabilityExecutionResult actual = this.getWorkspaceSitesCapabilityHandler.execute(givenCommand);

        //then
        assertThat(actual).isEqualTo(new CapabilityExecutionResult(CapabilityName.GET_WORKSPACE_SITES, givenPayload));
        verify(this.workspaceProjectionClient).getWorkspaceSites(17L);
    }

    @Test
    void givenDownstreamFailure_whenExecute_thenPropagateSameException() {
        //given
        final CapabilityExecutionCommand<WorkspaceSitesArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("9f13c2a3-3f83-43ba-bcb7-9842deaa95f9"),
                new WorkspaceSitesArg()
        );
        final IllegalStateException givenException = new IllegalStateException("downstream failure");
        when(this.workspaceProjectionClient.getWorkspaceSites(17L)).thenThrow(givenException);

        //when
        //then
        assertThatThrownBy(() -> this.getWorkspaceSitesCapabilityHandler.execute(givenCommand))
                .isSameAs(givenException);
        verify(this.workspaceProjectionClient).getWorkspaceSites(17L);
    }
}
