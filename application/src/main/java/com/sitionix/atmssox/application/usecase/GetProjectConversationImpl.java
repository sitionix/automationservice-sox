package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.GetProjectConversation;
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
public class GetProjectConversationImpl implements GetProjectConversation {

    private final AgentProjectRepository agentProjectRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public ProjectConversationDetails execute(final UUID projectId, final UUID conversationId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        final AgentProject project = this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final Conversation conversation = this.conversationRepository.findActiveByIdAndUserIdAndProjectId(conversationId, userId, projectId)
                .orElseThrow(() -> new AgentNotFoundException("Conversation not found"));

        final Map<UUID, ProjectAgent> projectAgentsById = this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId)
                .stream()
                .collect(Collectors.toMap(ProjectAgent::getId, Function.identity()));

        final List<ConversationParticipant> participants = this.conversationParticipantRepository.findAllByConversationId(conversation.getId())
                .stream()
                .filter(participant -> participant.getParticipantType() == ConversationParticipantType.AGENT)
                .map(participant -> {
                    final ProjectAgent projectAgent = projectAgentsById.get(UUID.fromString(participant.getParticipantId()));
                    return participant.toBuilder()
                            .name(projectAgent == null ? null : projectAgent.getName())
                            .description(projectAgent == null ? null : projectAgent.getDescription())
                            .status(projectAgent == null ? null : projectAgent.getStatus())
                            .build();
                })
                .toList();
        final List<ConversationMessage> messages = this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversation.getId());

        return ProjectConversationDetails.builder()
                .conversation(conversation)
                .project(project)
                .participants(participants)
                .messages(messages)
                .build();
    }
}
