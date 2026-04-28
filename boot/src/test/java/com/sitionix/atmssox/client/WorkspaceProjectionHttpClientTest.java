package com.sitionix.atmssox.client;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.dto.SiteOverviewDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WorkspaceProjectionHttpClientTest {

    private WorkspaceProjectionHttpClient workspaceProjectionHttpClient;

    private SiteApi siteApi;

    private ObjectMapper objectMapper;

    private WorkspaceProjectionClientCallExecutor workspaceProjectionClientCallExecutor;

    @BeforeEach
    void setUp() {
        this.siteApi = Mockito.mock(SiteApi.class);
        this.objectMapper = Mockito.mock(ObjectMapper.class);
        this.workspaceProjectionClientCallExecutor = new WorkspaceProjectionClientCallExecutor();
        this.workspaceProjectionHttpClient = new WorkspaceProjectionHttpClient(
                this.siteApi,
                this.objectMapper,
                this.workspaceProjectionClientCallExecutor
        );
    }

    @Test
    void givenValidArguments_whenGetSiteOverview_thenReturnMappedJsonNode() {
        //given
        final UUID givenSiteId = UUID.fromString("79bb6a6e-d2de-43fc-8f28-41a6f76a275f");
        final SiteOverviewDTO givenDto = Mockito.mock(SiteOverviewDTO.class);
        final JsonNode givenJson = new ObjectMapper().valueToTree(Map.of("siteId", givenSiteId.toString()));
        when(this.siteApi.getSiteOverview(givenSiteId)).thenReturn(givenDto);
        when(this.objectMapper.valueToTree(givenDto)).thenReturn(givenJson);

        //when
        final JsonNode actual = this.workspaceProjectionHttpClient.getSiteOverview(17L, givenSiteId);

        //then
        assertThat(actual).isEqualTo(givenJson);
        verify(this.siteApi).getSiteOverview(givenSiteId);
        verify(this.objectMapper).valueToTree(givenDto);
    }

    @Test
    void givenInvalidUserId_whenGetSiteOverview_thenThrowAgentValidationException() {
        //given
        final UUID givenSiteId = UUID.fromString("a7d57af8-fd3b-4840-9514-33dfbb0e60b0");

        //when
        //then
        assertThatThrownBy(() -> this.workspaceProjectionHttpClient.getSiteOverview(0L, givenSiteId))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("userId is required");
        verifyNoInteractions(this.siteApi);
        verifyNoInteractions(this.objectMapper);
    }
}
