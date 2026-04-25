package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAgentRuleImplTest {

    private CreateAgentRuleImpl createAgentRule;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.createAgentRule = new CreateAgentRuleImpl(
                this.agentRepository,
                this.agentRuleRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentRepository,
                this.agentRuleRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenValidInput_whenExecute_thenCreateActiveRuleWithTrimmedText() {
        //given
        final UUID agentId = UUID.fromString("a8e649b0-f56b-4f7f-84cb-33e0d90d6d3a");
        final CreateAgentRuleCommand command = new CreateAgentRuleCommand(
                "  Output style  ",
                "  Always be explicit.  ",
                AgentRuleStatus.ACTIVE,
                AgentRuleAuthorType.USER
        );
        final Agent agent = mock(Agent.class);
        final AgentRule saved = this.getRule(
                agentId,
                "Output style",
                "Always be explicit.",
                AgentRuleStatus.ACTIVE,
                AgentRuleAuthorType.USER
        );

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenReturn(saved);

        //when
        final AgentRule actual = this.createAgentRule.execute(agentId, command);

        //then
        assertThat(actual).isEqualTo(saved);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentRuleRepository).save(any(AgentRule.class));
    }

    @Test
    void givenBlankText_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("13c953ec-8da6-46f3-9390-2ab25a3ddfbe");
        final CreateAgentRuleCommand command = new CreateAgentRuleCommand(
                "   ",
                "valid content",
                AgentRuleStatus.ACTIVE,
                AgentRuleAuthorType.USER
        );
        final Agent agent = mock(Agent.class);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));

        //when
        //then
        assertThatThrownBy(() -> this.createAgentRule.execute(agentId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Rule title is required");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
    }

    @Test
    void givenUnknownAgent_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("27ec00b4-6cb9-4720-9949-11cd1d3f18d0");
        final CreateAgentRuleCommand command = new CreateAgentRuleCommand(
                "title",
                "rule",
                AgentRuleStatus.ACTIVE,
                AgentRuleAuthorType.USER
        );

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.createAgentRule.execute(agentId, command))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
    }

    private AgentRule getRule(final UUID agentId,
                              final String title,
                              final String content,
                              final AgentRuleStatus status,
                              final AgentRuleAuthorType authorType) {
        return AgentRule.builder()
                .id(UUID.fromString("b23f62f7-e3d8-4f9c-bd28-a911ad4d5007"))
                .agentId(agentId)
                .title(title)
                .content(content)
                .status(status)
                .authorType(authorType)
                .build();
    }
}
