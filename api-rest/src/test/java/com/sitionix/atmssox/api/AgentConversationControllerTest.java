package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import com.sitionix.atmssox.domain.usecase.CreateProjectConversation;
import com.sitionix.atmssox.domain.usecase.ListProjectConversations;
import com.sitionix.atmssox.domain.usecase.GetProjectConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentConversationControllerTest {

    private AgentConversationController agentConversationController;

    @Mock private GetAgentConversations getAgentConversations;
    @Mock private GetAgentConversation getAgentConversation;
    @Mock private DeleteAgentConversation deleteAgentConversation;
    @Mock private CreateProjectConversation createProjectConversation;
    @Mock private ListProjectConversations listProjectConversations;
    @Mock private GetProjectConversation getProjectConversation;
    @Mock private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentConversationController = new AgentConversationController(this.getAgentConversations, this.getAgentConversation, this.deleteAgentConversation, this.createProjectConversation, this.listProjectConversations, this.getProjectConversation, this.agentApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.getAgentConversations, this.getAgentConversation, this.deleteAgentConversation, this.createProjectConversation, this.listProjectConversations, this.getProjectConversation, this.agentApiMapper);
    }

    @Test
    void givenConversationId_whenDeleteAgentConversation_thenReturnNoContent() {
        //given
        final UUID conversationId = UUID.fromString("9f22ce1b-1286-493f-9f3a-a8f216f51bc7");

        //when
        final ResponseEntity<Void> actual = this.agentConversationController.deleteAgentConversation(conversationId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.noContent().build());
        verify(this.deleteAgentConversation).execute(conversationId);
    }

    @Test
    void givenAgentId_whenGetAgentConversations_thenReturnMappedResponse() {
        //given
        final UUID agentId = UUID.randomUUID();
        final List<Conversation> response = mock(List.class);
        final AgentConversationsResponseDTO responseDto = mock(AgentConversationsResponseDTO.class);
        when(this.getAgentConversations.execute(agentId)).thenReturn(response);
        when(this.agentApiMapper.asAgentConversationsResponseDto(response)).thenReturn(responseDto);

        //when
        final ResponseEntity<AgentConversationsResponseDTO> actual = this.agentConversationController.getAgentConversations(agentId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.getAgentConversations).execute(agentId);
        verify(this.agentApiMapper).asAgentConversationsResponseDto(response);
    }

    @Test
    void givenConversationId_whenGetAgentConversation_thenReturnMappedResponse() {
        //given
        final UUID conversationId = UUID.randomUUID();
        final ConversationDetails details = mock(ConversationDetails.class);
        final AgentConversationDetailsDTO responseDto = mock(AgentConversationDetailsDTO.class);
        when(this.getAgentConversation.execute(conversationId)).thenReturn(details);
        when(this.agentApiMapper.asAgentConversationDetailsDto(details)).thenReturn(responseDto);

        //when
        final ResponseEntity<AgentConversationDetailsDTO> actual = this.agentConversationController.getAgentConversation(conversationId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(responseDto));
        verify(this.getAgentConversation).execute(conversationId);
        verify(this.agentApiMapper).asAgentConversationDetailsDto(details);
    }
}
