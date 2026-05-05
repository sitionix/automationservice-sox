package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
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
    private static final int MAX_SIZE = 100;

    private final AgentProjectRepository agentProjectRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public AgentProjectsPage execute(final GetAgentProjectsQuery query) {
        final int page = this.resolvePage(query);
        final int size = this.resolveSize(query);
        return this.agentProjectRepository.findAllVisibleByOwnerUserId(
                this.authenticatedUserProvider.getUserId(),
                page,
                size
        );
    }

    private int resolvePage(final GetAgentProjectsQuery query) {
        final Integer page = query == null ? null : query.page();
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 0) {
            throw new AgentValidationException("Page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolveSize(final GetAgentProjectsQuery query) {
        final Integer size = query == null ? null : query.size();
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new AgentValidationException("Size must be between 1 and 100");
        }
        return size;
    }
}
