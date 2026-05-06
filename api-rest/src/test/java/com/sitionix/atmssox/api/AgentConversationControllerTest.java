package com.sitionix.atmssox.api;

import com.sitionix.atmssox.api.mapper.AgentApiMapper;
import com.sitionix.atmssox.domain.usecase.DeleteAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversation;
import com.sitionix.atmssox.domain.usecase.GetAgentConversations;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AgentConversationControllerTest {

    private AgentConversationController agentConversationController;

    @Mock private GetAgentConversations getAgentConversations;
    @Mock private GetAgentConversation getAgentConversation;
    @Mock private DeleteAgentConversation deleteAgentConversation;
    @Mock private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentConversationController = new AgentConversationController(this.getAgentConversations, this.getAgentConversation,
                this.deleteAgentConversation, this.agentApiMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.getAgentConversations, this.getAgentConversation, this.deleteAgentConversation, this.agentApiMapper);
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
}
