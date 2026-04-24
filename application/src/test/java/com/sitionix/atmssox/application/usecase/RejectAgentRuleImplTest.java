package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
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
class RejectAgentRuleImplTest {

    private RejectAgentRuleImpl rejectAgentRule;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.rejectAgentRule = new RejectAgentRuleImpl(
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
    void givenPendingAiRule_whenExecute_thenSetRejectedStatus() {
        //given
        final UUID agentId = UUID.fromString("3a32d3e2-a2fd-4bf3-8b31-0f0de9cc0c91");
        final UUID ruleId = UUID.fromString("096d9f3f-e2ba-4ff9-9b0d-faf8698fca1f");
        final AgentRule current = this.getRule(ruleId, agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.AI);
        final AgentRule updated = this.getRule(ruleId, agentId, AgentRuleStatus.REJECTED, AgentRuleAuthorType.AI);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));
        when(this.agentRuleRepository.save(any(AgentRule.class))).thenReturn(updated);

        //when
        final AgentRule actual = this.rejectAgentRule.execute(agentId, ruleId);

        //then
        assertThat(actual).isEqualTo(updated);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
        verify(this.agentRuleRepository).save(any(AgentRule.class));
    }

    @Test
    void givenUserRule_whenExecute_thenThrowLifecycleException() {
        //given
        final UUID agentId = UUID.fromString("d3ce4ddf-72dd-4bc0-8b9d-b4c38b2f703f");
        final UUID ruleId = UUID.fromString("3146fc9f-b2b3-48ea-8adb-4f1d298374b2");
        final AgentRule current = this.getRule(ruleId, agentId, AgentRuleStatus.PENDING, AgentRuleAuthorType.USER);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.rejectAgentRule.execute(agentId, ruleId))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("Only AI rule can be rejected");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentRuleRepository).findByIdAndAgentIdAndUserId(ruleId, agentId, 17L);
    }

    @Test
    void givenActiveAiRule_whenExecute_thenThrowLifecycleException() {
        //given
        final UUID agentId = UUID.fromString("69e98d06-9ec9-467d-8194-96ecf8af552f");
        final UUID ruleId = UUID.fromString("3f94e393-c66a-47a3-a6f6-ef7e05376195");
        final AgentRule current = this.getRule(ruleId, agentId, AgentRuleStatus.ACTIVE, AgentRuleAuthorType.AI);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, 17L)).thenReturn(Optional.of(current));

        //when
        //then
        assertThatThrownBy(() -> this.rejectAgentRule.execute(agentId, ruleId))
                .isInstanceOf(AgentLifecycleTransitionException.class)
                .hasMessage("Only PENDING AI rule can be rejected");
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
