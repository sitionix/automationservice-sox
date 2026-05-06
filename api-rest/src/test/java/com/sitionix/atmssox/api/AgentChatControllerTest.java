package com.sitionix.atmssox.api;

import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.usecase.GetAgentChatExecution;
import com.sitionix.atmssox.domain.usecase.SubmitAgentChatExecution;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentChatControllerTest {

    private AgentChatController agentChatController;

    @Mock private SubmitAgentChatExecution submitAgentChatExecution;
    @Mock private GetAgentChatExecution getAgentChatExecution;
    @Mock private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentChatController = new AgentChatController(this.submitAgentChatExecution, this.getAgentChatExecution, this.agentApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.submitAgentChatExecution, this.getAgentChatExecution, this.agentApiMapper);
    }

    @Test
    void givenSubmitExecutionRequest_whenSubmitAgentChatExecution_thenReturnAcceptedEnvelope() {
        //given
        final UUID agentId = UUID.fromString("76f023a2-cb0c-44d5-970d-053f4af51f6b");
        final ChatAgentRequestDTO request = mock(ChatAgentRequestDTO.class);
        final ChatAgentCommand command = mock(ChatAgentCommand.class);
        final ChatExecution execution = mock(ChatExecution.class);
        final SubmitChatExecutionResponseDTO response = mock(SubmitChatExecutionResponseDTO.class);
        when(this.agentApiMapper.asChatAgentCommand(request)).thenReturn(command);
        when(this.submitAgentChatExecution.execute(agentId, command, "idem")).thenReturn(execution);
        when(this.agentApiMapper.asSubmitChatExecutionResponseDto(execution)).thenReturn(response);

        //when
        final ResponseEntity<SubmitChatExecutionResponseDTO> actual = this.agentChatController.submitAgentChatExecution(agentId, request, "idem");

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
        verify(this.agentApiMapper).asChatAgentCommand(request);
        verify(this.submitAgentChatExecution).execute(agentId, command, "idem");
        verify(this.agentApiMapper).asSubmitChatExecutionResponseDto(execution);
    }

    @Test
    void givenExecutionLookupRequest_whenGetAgentChatExecution_thenReturnOkEnvelope() {
        //given
        final UUID agentId = UUID.fromString("1f723177-ec03-4506-9011-cf0b39c97c61");
        final UUID executionId = UUID.fromString("d67d95cb-8fcb-4a09-8f17-4ad5295a784b");
        final UUID conversationId = UUID.fromString("3b08ad4e-13f6-4d83-ab5d-dfe04ccebe4f");
        final ChatExecution execution = mock(ChatExecution.class);
        final ChatExecutionDTO response = mock(ChatExecutionDTO.class);
        when(this.getAgentChatExecution.execute(agentId, executionId, conversationId)).thenReturn(execution);
        when(this.agentApiMapper.asChatExecutionDto(execution)).thenReturn(response);

        //when
        final ResponseEntity<ChatExecutionDTO> actual = this.agentChatController.getAgentChatExecution(agentId, executionId, conversationId);

        //then
        assertThat(actual).isEqualTo(ResponseEntity.ok(response));
        verify(this.getAgentChatExecution).execute(agentId, executionId, conversationId);
        verify(this.agentApiMapper).asChatExecutionDto(execution);
    }

    @Test
    void givenSubmitExecutionByExecutionsPathRequest_whenSubmitAgentChatExecutionByExecutionsPath_thenReturnAcceptedEnvelope() {
        //given
        final UUID agentId = UUID.fromString("84de5bd8-e32d-469f-9f10-90f296ba7a92");
        final ChatAgentRequestDTO request = mock(ChatAgentRequestDTO.class);
        final ChatAgentCommand command = mock(ChatAgentCommand.class);
        final ChatExecution execution = mock(ChatExecution.class);
        final SubmitChatExecutionResponseDTO response = mock(SubmitChatExecutionResponseDTO.class);
        when(this.agentApiMapper.asChatAgentCommand(request)).thenReturn(command);
        when(this.submitAgentChatExecution.execute(agentId, command, "idem-2")).thenReturn(execution);
        when(this.agentApiMapper.asSubmitChatExecutionResponseDto(execution)).thenReturn(response);

        //when
        final ResponseEntity<SubmitChatExecutionResponseDTO> actual =
                this.agentChatController.submitAgentChatExecutionByExecutionsPath(agentId, request, "idem-2");

        //then
        assertThat(actual).isEqualTo(ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
        verify(this.agentApiMapper).asChatAgentCommand(request);
        verify(this.submitAgentChatExecution).execute(agentId, command, "idem-2");
        verify(this.agentApiMapper).asSubmitChatExecutionResponseDto(execution);
    }
}
