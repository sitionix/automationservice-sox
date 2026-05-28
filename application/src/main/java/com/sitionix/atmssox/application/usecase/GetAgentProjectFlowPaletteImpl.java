package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import com.sitionix.atmssox.domain.model.AgentProjectFlowPaletteSource;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectFlowPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentProjectFlowPaletteImpl implements GetAgentProjectFlowPalette {

    private static final String USER_SOURCE_TYPE = "USER";
    private static final String AGENT_SOURCE_TYPE = "AGENT";

    private final AgentProjectRepository agentProjectRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public AgentProjectFlowPalette execute(final UUID projectId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final List<ProjectAgent> projectAgents = this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId);
        final List<AgentProjectFlowPaletteSource> sources = new ArrayList<>();
        sources.add(AgentProjectFlowPaletteSource.builder()
                .sourceType(USER_SOURCE_TYPE)
                .sourceId(null)
                .sourceName("User")
                .build());
        for (final ProjectAgent projectAgent : projectAgents) {
            sources.add(AgentProjectFlowPaletteSource.builder()
                    .sourceType(AGENT_SOURCE_TYPE)
                    .sourceId(projectAgent.getId())
                    .sourceName(projectAgent.getName())
                    .build());
        }

        return AgentProjectFlowPalette.builder().sources(sources).build();
    }
}
