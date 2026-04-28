package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.CapabilityProperty;
import com.sitionix.atmssox.domain.model.capability.SiteOverviewArg;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSiteOverviewCapabilityHandlerTest {

    private GetSiteOverviewCapabilityHandler getSiteOverviewCapabilityHandler;

    @Mock
    private WorkspaceProjectionClient workspaceProjectionClient;

    @BeforeEach
    void setUp() {
        this.getSiteOverviewCapabilityHandler = new GetSiteOverviewCapabilityHandler(this.workspaceProjectionClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenDefinition_whenCalled_thenReturnExpectedSchemaFields() {
        //given

        //when
        final CapabilityDefinition actual = this.getSiteOverviewCapabilityHandler.definition();

        //then
        final CapabilityProperty property = actual.inputSchema().properties().get("siteId");
        assertThat(actual.name()).isEqualTo(CapabilityName.GET_SITE_OVERVIEW.name());
        assertThat(property.type()).isEqualTo("string");
        assertThat(property.format()).isEqualTo("uuid");
        assertThat(property.description()).isEqualTo("Site identifier");
        assertThat(actual.inputSchema().required()).isEqualTo(List.of("siteId"));
        assertThat(actual.inputSchema().additionalProperties()).isEqualTo(Boolean.FALSE);
    }

    @Test
    void givenDefinition_whenSerializedToJson_thenReturnOpenAiCompatibleSchemaShape() {
        //given
        final ObjectMapper objectMapper = new ObjectMapper();
        final CapabilityDefinition definition = this.getSiteOverviewCapabilityHandler.definition();

        //when
        final JsonNode actual = objectMapper.valueToTree(definition.inputSchema());

        //then
        final JsonNode expected = objectMapper.valueToTree(Map.of(
                "type", "object",
                "properties", Map.of(
                        "siteId", Map.of(
                                "type", "string",
                                "format", "uuid",
                                "description", "Site identifier"
                        )
                ),
                "required", List.of("siteId"),
                "additionalProperties", Boolean.FALSE
        ));
        assertThat(actual).isEqualTo(expected);
        verifyNoInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenNullCommand_whenExecute_thenThrowAgentValidationException() {
        //given

        //when
        //then
        assertThatThrownBy(() -> this.getSiteOverviewCapabilityHandler.execute(null))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Site identifier is required");
        verifyNoInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenNullArgNode_whenExecute_thenThrowAgentValidationException() {
        //given
        final CapabilityExecutionCommand<SiteOverviewArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("2ee1a7bb-39ef-4db9-b228-ffbea100f86f"),
                null
        );

        //when
        //then
        assertThatThrownBy(() -> this.getSiteOverviewCapabilityHandler.execute(givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Site identifier is required");
        verifyNoInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenNullSiteId_whenExecute_thenThrowAgentValidationException() {
        //given
        final CapabilityExecutionCommand<SiteOverviewArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("3be2942e-a2e5-4f92-8f5e-e4a2f59745ef"),
                new SiteOverviewArg(null)
        );

        //when
        //then
        assertThatThrownBy(() -> this.getSiteOverviewCapabilityHandler.execute(givenCommand))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Site identifier is required");
        verifyNoInteractions(this.workspaceProjectionClient);
    }

    @Test
    void givenValidCommand_whenExecute_thenReturnCapabilityExecutionResultAndCallWorkspaceProjectionClient() {
        //given
        final UUID givenSiteId = UUID.fromString("7b8473ef-d2c4-4efa-a73b-93eb5f6db0fd");
        final CapabilityExecutionCommand<SiteOverviewArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("6ce53195-dd6a-48b7-b9f2-7f7409249f68"),
                new SiteOverviewArg(givenSiteId)
        );
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode givenPayload = objectMapper.valueToTree(Map.of("siteId", givenSiteId.toString()));
        when(this.workspaceProjectionClient.getSiteOverview(17L, givenSiteId)).thenReturn(givenPayload);

        //when
        final CapabilityExecutionResult actual = this.getSiteOverviewCapabilityHandler.execute(givenCommand);

        //then
        assertThat(actual).isEqualTo(new CapabilityExecutionResult(CapabilityName.GET_SITE_OVERVIEW, givenPayload));
        verify(this.workspaceProjectionClient).getSiteOverview(17L, givenSiteId);
    }

    @Test
    void givenWorkspaceProjectionClientThrows_whenExecute_thenPropagateSameException() {
        //given
        final UUID givenSiteId = UUID.fromString("f4018be2-fd2b-4442-afcd-e76927fb60b7");
        final CapabilityExecutionCommand<SiteOverviewArg> givenCommand = new CapabilityExecutionCommand<>(
                17L,
                22L,
                UUID.fromString("f7e8446f-290e-4e08-9744-e9a857f5bcc1"),
                new SiteOverviewArg(givenSiteId)
        );
        final IllegalStateException givenException = new IllegalStateException("downstream failure");
        when(this.workspaceProjectionClient.getSiteOverview(17L, givenSiteId)).thenThrow(givenException);

        //when
        //then
        assertThatThrownBy(() -> this.getSiteOverviewCapabilityHandler.execute(givenCommand))
                .isSameAs(givenException);
        verify(this.workspaceProjectionClient).getSiteOverview(17L, givenSiteId);
    }
}
