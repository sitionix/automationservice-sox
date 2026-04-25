package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAgentRuleImplTest {

    private DeleteAgentRuleImpl deleteAgentRule;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.deleteAgentRule = new DeleteAgentRuleImpl(
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
    void givenActiveRule_whenExecute_thenSoftDeleteAndReturnDeletedStatus() {
        //given
        final UUID agentId = UUID.fromString("f88b5e50-2cb9-4218-ba2e-0983d1a01ca0");
        final UUID ruleId = UUID.fromString("894d47f5-9b80-407b-b918-5f1f18bc9ea0");
        final AgentRule active = this.getRule(ruleId, agentId, AgentRuleStatus.ACTIVE);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(active));
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        final DeleteAgentRuleResponse actual = this.deleteAgentRule.execute(agentId, ruleId);

        //then
        assertThat(actual.status()).isEqualTo(AgentRuleStatus.DELETED);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
        verify(this.agentRuleRepository).save(any(AgentRule.class));
    }

    @Test
    void givenDeletedRule_whenExecute_thenThrowLifecycleExceptionWithoutSave() {
        //given
        final UUID agentId = UUID.fromString("72336f45-c901-4adf-8ecf-c88d47115a1a");
        final UUID ruleId = UUID.fromString("f4b539f8-f769-4f94-a2cf-c35f57cad4f8");
        final AgentRule deleted = this.getRule(ruleId, agentId, AgentRuleStatus.DELETED);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(deleted));

        //when
        assertThatThrownBy(() -> this.deleteAgentRule.execute(agentId, ruleId))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("Rule is already DELETED");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
        verify(this.agentRuleRepository, never()).save(any(AgentRule.class));
    }

    @Test
    void givenRuleNotFound_whenExecute_thenThrowNotFoundException() {
        //given
        final UUID agentId = UUID.fromString("2bfa0c4e-f885-41bc-a6eb-340727500c4a");
        final UUID ruleId = UUID.fromString("9ee9db96-3e36-4f10-8e0f-2837f79f705e");

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.deleteAgentRule.execute(agentId, ruleId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent rule not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    private AgentRule getRule(final UUID ruleId, final UUID agentId, final AgentRuleStatus status) {
        return AgentRule.builder()
                .id(ruleId)
                .agentId(agentId)
                .title("rule")
                .content("rule content")
                .status(status)
                .authorType(AgentRuleAuthorType.USER)
                .build();
    }
}
