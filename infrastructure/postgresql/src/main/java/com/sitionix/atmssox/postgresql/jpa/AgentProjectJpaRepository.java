package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProjectJpaRepository extends JpaRepository<AgentProjectEntity, UUID> {

    Page<AgentProjectEntity> findByOwnerUserIdAndStatusIdNot(Long ownerUserId,
                                                           Long statusId,
                                                           Pageable pageable);
}
