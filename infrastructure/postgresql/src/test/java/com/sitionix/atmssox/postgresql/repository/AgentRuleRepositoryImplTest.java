package com.sitionix.atmssox.postgresql.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleStatusEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentRuleJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentRuleRepositoryImplTest {

    private AgentRuleRepositoryImpl agentRuleRepository;

    @Mock
    private AgentRuleJpaRepository agentRuleJpaRepository;

    @BeforeEach
    void setUp() {
        this.agentRuleRepository = new AgentRuleRepositoryImpl(this.agentRuleJpaRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.agentRuleJpaRepository);
    }

    @Test
    void givenAgentRule_whenSave_thenReturnMappedDomainRule() {
        //given
        final AgentRule given = this.getAgentRule(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Always validate input",
                AgentRuleStatus.ACTIVE,
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:00:00Z")
        );

        final AgentRuleEntity persisted = this.getAgentRuleEntity(
                given.getId(),
                given.getAgentId(),
                given.getText(),
                given.getStatus(),
                given.getCreatedAt(),
                given.getUpdatedAt()
        );

        when(this.agentRuleJpaRepository.save(any(AgentRuleEntity.class))).thenReturn(persisted);

        //when
        final AgentRule actual = this.agentRuleRepository.save(given);

        //then
        assertThat(actual).isEqualTo(given);
        verify(this.agentRuleJpaRepository).save(any(AgentRuleEntity.class));
    }

    @Test
    void givenAgentIdUserIdAndStatus_whenFindAllByAgentIdAndUserIdAndStatusOrderByCreatedAtAsc_thenReturnMappedRules() {
        //given
        final UUID agentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final Long userId = 17L;
        final AgentRuleStatus status = AgentRuleStatus.ACTIVE;

        final AgentRuleEntity first = this.getAgentRuleEntity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                agentId,
                "First",
                AgentRuleStatus.ACTIVE,
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final AgentRuleEntity second = this.getAgentRuleEntity(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                agentId,
                "Second",
                AgentRuleStatus.ACTIVE,
                Instant.parse("2026-04-21T10:01:00Z"),
                Instant.parse("2026-04-21T10:01:00Z")
        );

        final List<AgentRule> expected = List.of(
                this.getAgentRule(first.getRuleId(), agentId, "First", AgentRuleStatus.ACTIVE, first.getCreatedAt(), first.getUpdatedAt()),
                this.getAgentRule(second.getRuleId(), agentId, "Second", AgentRuleStatus.ACTIVE, second.getCreatedAt(), second.getUpdatedAt())
        );

        when(this.agentRuleJpaRepository.findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(agentId, userId, status.getId()))
                .thenReturn(List.of(first, second));

        //when
        final List<AgentRule> actual = this.agentRuleRepository.findAllByAgentIdAndUserIdAndStatusOrderByCreatedAtAsc(agentId, userId, status);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentRuleJpaRepository).findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(agentId, userId, status.getId());
    }

    @Test
    void givenRuleIdAgentIdAndUserId_whenFindByIdAndAgentIdAndUserIdExists_thenReturnMappedRule() {
        //given
        final UUID ruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID agentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final Long userId = 17L;
        final AgentRuleEntity entity = this.getAgentRuleEntity(
                ruleId,
                agentId,
                "Rule",
                AgentRuleStatus.ACTIVE,
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final Optional<AgentRule> expected = Optional.of(
                this.getAgentRule(ruleId, agentId, "Rule", AgentRuleStatus.ACTIVE, entity.getCreatedAt(), entity.getUpdatedAt())
        );

        when(this.agentRuleJpaRepository.findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId))
                .thenReturn(Optional.of(entity));

        //when
        final Optional<AgentRule> actual = this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, userId);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.agentRuleJpaRepository).findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId);
    }

    @Test
    void givenRuleIdAgentIdAndUserId_whenFindByIdAndAgentIdAndUserIdMissing_thenReturnEmpty() {
        //given
        final UUID ruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID agentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final Long userId = 17L;

        when(this.agentRuleJpaRepository.findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId))
                .thenReturn(Optional.empty());

        //when
        final Optional<AgentRule> actual = this.agentRuleRepository.findByIdAndAgentIdAndUserId(ruleId, agentId, userId);

        //then
        assertThat(actual).isEqualTo(Optional.empty());
        verify(this.agentRuleJpaRepository).findByRuleIdAndAgentAgentIdAndAgentUserId(ruleId, agentId, userId);
    }

    private AgentRule getAgentRule(final UUID id,
                                   final UUID agentId,
                                   final String text,
                                   final AgentRuleStatus status,
                                   final Instant createdAt,
                                   final Instant updatedAt) {
        return AgentRule.builder()
                .id(id)
                .agentId(agentId)
                .text(text)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private AgentRuleEntity getAgentRuleEntity(final UUID id,
                                               final UUID agentId,
                                               final String text,
                                               final AgentRuleStatus status,
                                               final Instant createdAt,
                                               final Instant updatedAt) {
        final AgentEntity agentEntity = new AgentEntity();
        agentEntity.setAgentId(agentId);
        final AgentRuleStatusEntity statusEntity = AgentRuleStatusEntity.builder()
                .id(status.getId())
                .description(status.name())
                .build();

        return AgentRuleEntity.builder()
                .ruleId(id)
                .agent(agentEntity)
                .text(text)
                .status(statusEntity)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
