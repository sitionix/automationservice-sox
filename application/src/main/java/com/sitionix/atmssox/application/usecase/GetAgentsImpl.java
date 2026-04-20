package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.GetAgents;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAgentsImpl implements GetAgents {

    private final AgentRepository agentRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public List<Agent> execute() {
        return this.agentRepository.findAllVisibleByUserId(this.authenticatedUserProvider.getUserId());
    }
}
