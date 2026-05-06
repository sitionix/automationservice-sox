package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.model.AgentProjectsPage;
import com.sitionix.atmssox.domain.model.GetAgentProjectsQuery;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentProjects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentProjectsImpl implements GetAgentProjects {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private final AgentProjectRepository agentProjectRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public AgentProjectsPage execute(final GetAgentProjectsQuery query) {
        final Integer requestedPage = query == null ? null : query.page();
        final Integer requestedSize = query == null ? null : query.size();
        final int page = requestedPage == null ? DEFAULT_PAGE : requestedPage;
        final int size = requestedSize == null ? DEFAULT_SIZE : requestedSize;
        return this.agentProjectRepository.findAllVisibleByOwnerUserId(
                this.authenticatedUserProvider.getUserId(),
                page,
                size
        );
    }
}
