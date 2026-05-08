package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.ListProjectConversations;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListProjectConversationsImpl implements ListProjectConversations {

    private final AgentProjectRepository agentProjectRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectConversationDetails> execute(final UUID projectId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final AgentProject project = this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final Map<UUID, ProjectAgent> projectAgentsById = this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId)
                .stream()
                .collect(Collectors.toMap(ProjectAgent::getId, Function.identity()));

        return this.conversationRepository.findAllActiveByUserIdAndProjectId(userId, projectId)
                .stream()
                .map(conversation -> this.asProjectConversationDetails(project, conversation, projectAgentsById))
                .toList();
    }

    private ProjectConversationDetails asProjectConversationDetails(final AgentProject project,
                                                                    final Conversation conversation,
                                                                    final Map<UUID, ProjectAgent> projectAgentsById) {
        final List<ConversationParticipant> participants = this.conversationParticipantRepository.findAllByConversationId(conversation.getId())
                .stream()
                .filter(participant -> participant.getParticipantType() == com.sitionix.atmssox.domain.model.ConversationParticipantType.AGENT)
                .map(participant -> {
                    final ProjectAgent projectAgent = projectAgentsById.get(UUID.fromString(participant.getParticipantId()));
                    return participant.toBuilder()
                            .name(projectAgent == null ? null : projectAgent.getName())
                            .description(projectAgent == null ? null : projectAgent.getDescription())
                            .status(projectAgent == null ? null : projectAgent.getStatus())
                            .build();
                })
                .toList();
        return ProjectConversationDetails.builder()
                .conversation(conversation)
                .project(project)
                .participants(participants)
                .build();
    }
}
