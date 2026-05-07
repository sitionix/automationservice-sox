package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentProjectMemberJpaRepository extends JpaRepository<AgentProjectMemberEntity, UUID> {

    @Query("""
            SELECT member
            FROM AgentProjectMemberEntity member
            WHERE member.project.projectId = :projectId
              AND member.agent.agentId = :agentId
            """)
    Optional<AgentProjectMemberEntity> findByProjectIdAndAgentId(@Param("projectId") UUID projectId,
                                                                  @Param("agentId") UUID agentId);

    @Query("""
            SELECT member
            FROM AgentProjectMemberEntity member
            WHERE member.project.projectId = :projectId
              AND member.agent.agentId = :agentId
              AND member.status.id = :activeStatusId
            """)
    Optional<AgentProjectMemberEntity> findActiveByProjectIdAndAgentId(@Param("projectId") UUID projectId,
                                                                        @Param("agentId") UUID agentId,
                                                                        @Param("activeStatusId") Long activeStatusId);

    @Query("""
            SELECT member
            FROM AgentProjectMemberEntity member
            WHERE member.project.projectId = :projectId
              AND member.project.ownerUserId = :ownerUserId
              AND member.project.status.id <> :deletedProjectStatusId
              AND member.agent.userId = :ownerUserId
              AND member.agent.status.id <> :deletedAgentStatusId
              AND member.status.id = :activeMemberStatusId
            ORDER BY member.createdAt DESC
            """)
    List<AgentProjectMemberEntity> findVisibleProjectAgents(@Param("projectId") UUID projectId,
                                                            @Param("ownerUserId") Long ownerUserId,
                                                            @Param("deletedProjectStatusId") Long deletedProjectStatusId,
                                                            @Param("deletedAgentStatusId") Long deletedAgentStatusId,
                                                            @Param("activeMemberStatusId") Long activeMemberStatusId);
}
