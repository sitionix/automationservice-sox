package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, UUID> {

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.userId = ?1
              AND agent.type.id = 1
              AND agent.status.id <> ?2
            ORDER BY agent.updatedAt DESC
            """)
    List<AgentEntity> findAllVisibleUserAgentsByUserIdOrderByUpdatedAtDesc(Long userId, Long statusId);

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.agentId = ?1
              AND agent.userId = ?2
              AND agent.type.id = 1
              AND agent.status.id <> ?3
            """)
    Optional<AgentEntity> findVisibleUserAgentByAgentIdAndUserId(UUID agentId, Long userId, Long statusId);

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.agentId = ?1
              AND agent.userId = ?2
              AND agent.type.id = 1
            """)
    Optional<AgentEntity> findUserAgentByAgentIdAndUserId(UUID agentId, Long userId);

    Optional<AgentEntity> findByAgentId(UUID agentId);

    Optional<AgentEntity> findFirstByTypeIdOrderByCreatedAtAsc(Long typeId);
}
