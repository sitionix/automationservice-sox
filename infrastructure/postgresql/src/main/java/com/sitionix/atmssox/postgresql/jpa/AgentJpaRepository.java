package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentJpaRepository extends JpaRepository<AgentEntity, UUID> {

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.userId = :userId
              AND agent.type.id = 1
              AND agent.status.id <> :statusId
            ORDER BY agent.updatedAt DESC
            """)
    List<AgentEntity> findAllByUserIdAndStatusIdNotOrderByUpdatedAtDesc(@Param("userId") Long userId,
                                                                         @Param("statusId") Long statusId);

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.agentId = :agentId
              AND agent.userId = :userId
              AND agent.type.id = 1
              AND agent.status.id <> :statusId
            """)
    Optional<AgentEntity> findByAgentIdAndUserIdAndStatusIdNot(@Param("agentId") UUID agentId,
                                                                @Param("userId") Long userId,
                                                                @Param("statusId") Long statusId);

    @Query("""
            SELECT agent
            FROM AgentEntity agent
            WHERE agent.agentId = :agentId
              AND agent.userId = :userId
              AND agent.type.id = 1
            """)
    Optional<AgentEntity> findByAgentIdAndUserId(@Param("agentId") UUID agentId,
                                                  @Param("userId") Long userId);

    Optional<AgentEntity> findByAgentId(UUID agentId);

    Optional<AgentEntity> findByAgentIdAndTypeIdAndStatusId(UUID agentId, Long typeId, Long statusId);

    Optional<AgentEntity> findFirstByTypeIdOrderByCreatedAtAsc(Long typeId);
}
