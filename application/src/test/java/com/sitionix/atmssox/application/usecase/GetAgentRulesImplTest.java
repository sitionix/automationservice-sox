package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgentRulesImplTest {

    private GetAgentRulesImpl getAgentRules;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getAgentRules = new GetAgentRulesImpl(
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
    void givenVisibleAgent_whenExecute_thenReturnActiveRulesSortedFromRepository() {
        //given
        final UUID agentId = UUID.fromString("88d40603-c67f-4f33-b1d1-a5f66b4f9f57");
        final Agent agent = mock(Agent.class);
        final List<AgentRule> expected = List.of(this.getRule(agentId, "Rule 1"), this.getRule(agentId, "Rule 2"));

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndStatusOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE))
                .thenReturn(expected);

        //when
        final List<AgentRule> actual = this.getAgentRules.execute(agentId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndStatusOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE);
    }

    @Test
    void givenAgentNotFound_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("8b3399eb-4f0c-465e-be9d-5d0350dae364");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getAgentRules.execute(agentId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
    }

    private AgentRule getRule(final UUID agentId, final String text) {
        return AgentRule.builder()
                .id(UUID.randomUUID())
                .agentId(agentId)
                .text(text)
                .status(AgentRuleStatus.ACTIVE)
                .build();
    }
}
