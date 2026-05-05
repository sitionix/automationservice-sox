package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentProjectJpaRepository extends JpaRepository<AgentProjectEntity, UUID> {

    Page<AgentProjectEntity> findByOwnerUserIdAndStatusNot(Long ownerUserId,
                                                           AgentProjectStatus status,
                                                           Pageable pageable);
}
