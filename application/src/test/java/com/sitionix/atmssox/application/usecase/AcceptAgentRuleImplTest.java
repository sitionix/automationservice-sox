package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptAgentRuleImplTest {

    private AcceptAgentRuleImpl acceptAgentRule;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.acceptAgentRule = new AcceptAgentRuleImpl(
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
    void givenPendingRuleAndAcceptCommand_whenExecute_thenActivateRuleWithUpdatedText() {
        //given
        final UUID agentId = UUID.fromString("f145b460-14ee-43f2-b7f7-47d179d4ece1");
        final UUID ruleId = UUID.fromString("f14e7d10-d7f0-4149-bf4d-b8f15f350278");
        final AcceptAgentRuleCommand command = new AcceptAgentRuleCommand(
                "  Updated title  ",
                "  Updated content  "
        );
        final AgentRule current = this.getRule(ruleId, agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        final AgentRule updated = this.getRule(ruleId, agentId, AgentRuleStatus.ACTIVE, AgentRuleAuthorType.AI)
                .toBuilder()
                .title("Updated title")
                .content("Updated content")
                .build();

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenReturn(updated);

        //when
        final AgentRule actual = this.acceptAgentRule.execute(agentId, ruleId, command);

        //then
        assertThat(actual).isEqualTo(updated);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
        verify(this.agentRuleRepository).save(any(AgentRule.class));
    }

    @Test
    void givenActiveRule_whenExecute_thenThrowLifecycleException() {
        //given
        final UUID agentId = UUID.fromString("19e89f6f-f67e-468e-b667-4ba8ab18f391");
        final UUID ruleId = UUID.fromString("338d1b18-f770-4289-b1ca-acf7961a6f08");
        final AgentRule current = this.getRule(ruleId, agentId, AgentRuleStatus.ACTIVE, AgentRuleAuthorType.USER);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.acceptAgentRule.execute(agentId, ruleId, null))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("ACTIVE rule cannot be accepted");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    private AgentRule getRule(final UUID ruleId,
                              final UUID agentId,
                              final AgentRuleStatus status,
                              final AgentRuleAuthorType authorType) {
        return AgentRule.builder()
                .id(ruleId)
                .agentId(agentId)
                .title("Rule title")
                .content("Rule content")
                .status(status)
                .authorType(authorType)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:00:00Z"))
                .build();
    }
}
