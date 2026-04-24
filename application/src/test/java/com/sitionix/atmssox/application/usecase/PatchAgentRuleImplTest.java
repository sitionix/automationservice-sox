package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatchAgentRuleImplTest {

    private PatchAgentRuleImpl patchAgentRule;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.patchAgentRule = new PatchAgentRuleImpl(
                this.agentRuleRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentRuleRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenActiveRuleAndValidText_whenExecute_thenUpdateRuleText() {
        //given
        final UUID agentId = UUID.fromString("09bd93f6-3a23-4870-9ce0-aa0b8daa903f");
        final UUID ruleId = UUID.fromString("35773995-c66d-44f9-87f3-7b2bf34264f6");
        final PatchAgentRuleCommand command = new PatchAgentRuleCommand("  Updated rule text  ");
        final AgentRule current = this.getRule(ruleId, agentId, "Old text", AgentRuleStatus.ACTIVE);
        final AgentRule updated = this.getRule(ruleId, agentId, "Updated rule text", AgentRuleStatus.ACTIVE);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenReturn(updated);

        //when
        final AgentRule actual = this.patchAgentRule.execute(agentId, ruleId, command);

        //then
        assertThat(actual).isEqualTo(updated);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
        verify(this.agentRuleRepository).save(any(AgentRule.class));
    }

    @Test
    void givenDeletedRule_whenExecute_thenThrowLifecycleException() {
        //given
        final UUID agentId = UUID.fromString("7514f48a-bb35-4d8e-b9eb-e9a5f41e119a");
        final UUID ruleId = UUID.fromString("ef7f7b46-b174-45ce-b95f-9d01afb1d539");
        final PatchAgentRuleCommand command = new PatchAgentRuleCommand("updated");
        final AgentRule current = this.getRule(ruleId, agentId, "Old text", AgentRuleStatus.DELETED);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgentRule.execute(agentId, ruleId, command))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("Only ACTIVE rule can be updated");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    @Test
    void givenBlankText_whenExecute_thenThrowValidationException() {
        //given
        final UUID agentId = UUID.fromString("21757f2c-c883-49ef-96f5-c62916dff557");
        final UUID ruleId = UUID.fromString("e8f65c75-1f10-48a8-84ab-95af4a1a258b");
        final PatchAgentRuleCommand command = new PatchAgentRuleCommand("   ");
        final AgentRule current = this.getRule(ruleId, agentId, "Old text", AgentRuleStatus.ACTIVE);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.patchAgentRule.execute(agentId, ruleId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Rule text is required");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    @Test
    void givenRuleNotFound_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("9d46d570-69e5-4f5e-a520-fba542f67c7a");
        final UUID ruleId = UUID.fromString("a6db84b4-af5d-4f18-8c40-826c58b2f286");
        final PatchAgentRuleCommand command = new PatchAgentRuleCommand("updated");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.patchAgentRule.execute(agentId, ruleId, command))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent rule not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    private AgentRule getRule(final UUID ruleId, final UUID agentId, final String text, final AgentRuleStatus status) {
        return AgentRule.builder()
                .id(ruleId)
                .agentId(agentId)
                .text(text)
                .status(status)
                .build();
    }
}
