package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentExecutionServiceTest {

    private AgentExecutionService agentExecutionService;

    @Mock
    private AgentExecutionHandler<UserAgentExecutionContext> userAgentExecutionHandler;

    @BeforeEach
    void setUp() {
        this.agentExecutionService = new AgentExecutionService();
        AgentType.USER.setHandler(this.userAgentExecutionHandler);
    }

    @AfterEach
    void tearDown() {
        AgentType.USER.setHandler(null);
        verifyNoMoreInteractions(this.userAgentExecutionHandler);
    }

    @Test
    void givenAgentAndContext_whenExecute_thenDelegateToAgentTypeHandler() {
        //given
        final Agent givenAgent = this.getAgent();
        final UserAgentExecutionContext givenContext = new UserAgentExecutionContext("Prompt");
        when(this.userAgentExecutionHandler.executeWithContext(any(Agent.class), any(UserAgentExecutionContext.class))).thenReturn("reply");

        //when
        final String actual = this.agentExecutionService.execute(givenAgent, givenContext);

        //then
        assertThat(actual).isEqualTo("reply");
        verify(this.userAgentExecutionHandler).executeWithContext(givenAgent, givenContext);
    }

    private Agent getAgent() {
        return Agent.builder()
                .id(UUID.fromString("0b6e2964-56c3-479f-a220-a413aa8d6fb5"))
                .userId(17L)
                .name("Name")
                .description("Description")
                .instruction("Instruction")
                .type(AgentType.USER)
                .status(AgentStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }
}
