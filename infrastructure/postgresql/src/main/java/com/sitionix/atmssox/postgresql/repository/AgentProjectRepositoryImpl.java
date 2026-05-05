package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentProjectInfraMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentProjectRepositoryImpl implements AgentProjectRepository {

    private final AgentProjectJpaRepository agentProjectJpaRepository;
    private final AgentProjectInfraMapper agentProjectInfraMapper;

    @Override
    public AgentProject save(final AgentProject project) {
        final AgentProjectEntity entity = this.agentProjectJpaRepository.save(
                this.agentProjectInfraMapper.asAgentProjectEntity(project)
        );
        return this.agentProjectInfraMapper.asAgentProject(entity);
    }

    @Override
    public AgentProjectsPage findAllVisibleByOwnerUserId(final Long ownerUserId, final int page, final int size) {
        final Sort sort = Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("projectId")
        );
        final Page<AgentProjectEntity> response = this.agentProjectJpaRepository.findByOwnerUserIdAndStatusNot(
                ownerUserId,
                AgentProjectStatus.DELETED,
                PageRequest.of(page, size, sort)
        );
        final List<AgentProject> items = response.getContent().stream()
                .map(this.agentProjectInfraMapper::asAgentProject)
                .toList();

        return AgentProjectsPage.builder()
                .items(items)
                .page(page)
                .size(size)
                .hasNext(response.hasNext())
                .build();
    }
}
