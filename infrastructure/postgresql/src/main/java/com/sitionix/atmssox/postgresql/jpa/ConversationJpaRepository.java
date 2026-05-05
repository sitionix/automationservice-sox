package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationJpaRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("""
            select c
            from ConversationEntity c
            where c.status = :status
              and c.userId = :userId
              and exists (
                  select 1
                  from ConversationParticipantEntity p
                  where p.conversation = c
                    and p.participantType = :agentType
                    and p.participantRef = :agentRef
              )
            order by c.lastMessageAt desc
            """)
    List<ConversationEntity> findAllActiveByUserIdAndAgent(@Param("status") ConversationStatus status,
                                                           @Param("userId") Long userId,
                                                           @Param("agentType") ConversationParticipantType agentType,
                                                           @Param("agentRef") String agentRef);

    @Query("""
            select c
            from ConversationEntity c
            where c.conversationId = :conversationId
              and c.status = :status
              and c.userId = :userId
            """)
    Optional<ConversationEntity> findActiveByIdAndUserId(@Param("conversationId") UUID conversationId,
                                                          @Param("status") ConversationStatus status,
                                                          @Param("userId") Long userId);

    @Query("""
            select c
            from ConversationEntity c
            where c.conversationId = :conversationId
              and c.userId = :userId
            """)
    Optional<ConversationEntity> findByIdAndUserId(@Param("conversationId") UUID conversationId,
                                                    @Param("userId") Long userId);

    @Query("""
            select c
            from ConversationEntity c
            where c.conversationId = :conversationId
              and c.status = :status
              and c.userId = :userId
              and exists (
                  select 1
                  from ConversationParticipantEntity p
                  where p.conversation = c
                    and p.participantType = :agentType
                    and p.participantRef = :agentRef
              )
            """)
    Optional<ConversationEntity> findActiveByIdAndUserIdAndAgent(@Param("conversationId") UUID conversationId,
                                                                  @Param("status") ConversationStatus status,
                                                                  @Param("userId") Long userId,
                                                                  @Param("agentType") ConversationParticipantType agentType,
                                                                  @Param("agentRef") String agentRef);

    @Query("""
            select c
            from ConversationEntity c
            where c.conversationId = :conversationId
              and c.status = :status
              and exists (
                  select 1
                  from ConversationParticipantEntity p
                  where p.conversation = c
                    and p.participantType = :agentType
                    and p.participantRef = :agentRef
              )
            """)
    Optional<ConversationEntity> findActiveByIdAndAgent(@Param("conversationId") UUID conversationId,
                                                         @Param("status") ConversationStatus status,
                                                         @Param("agentType") ConversationParticipantType agentType,
                                                         @Param("agentRef") String agentRef);
}
