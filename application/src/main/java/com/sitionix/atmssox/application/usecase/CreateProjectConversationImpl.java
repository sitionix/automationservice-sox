package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import com.sitionix.atmssox.domain.usecase.CreateProjectConversation;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProjectConversationImpl implements CreateProjectConversation {

    private final AgentProjectRepository agentProjectRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public ProjectConversationDetails execute(final UUID projectId, final CreateProjectConversationCommand command) {
        if (command == null || command.getAgentIds() == null || command.getAgentIds().isEmpty()) {
            throw new AgentValidationException("agentIds must contain at least one item");
        }
        if (new HashSet<>(command.getAgentIds()).size() != command.getAgentIds().size()) {
            throw new AgentValidationException("agentIds must contain unique items");
        }

        final Long userId = this.authenticatedUserProvider.getUserId();
        final AgentProject project = this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final Map<UUID, ProjectAgent> attachedAgents = this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId)
                .stream()
                .collect(Collectors.toMap(ProjectAgent::getId, Function.identity()));

        final List<ProjectAgent> selectedAgents = command.getAgentIds().stream()
                .map(agentId -> {
                    final ProjectAgent projectAgent = attachedAgents.get(agentId);
                    if (projectAgent == null) {
                        throw new AgentNotFoundException("Agent not found");
                    }
                    return projectAgent;
                })
                .toList();

        final Instant now = Instant.now();
        final Conversation conversation = this.conversationRepository.save(Conversation.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .projectId(projectId)
                .title(this.resolveTitle(selectedAgents))
                .type(selectedAgents.size() == 1 ? ConversationType.DIRECT : ConversationType.MULTI_AGENT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastMessageAt(null)
                .build());

        final ConversationParticipant userParticipant = ConversationParticipant.builder()
                .id(UUID.randomUUID())
                .conversationId(conversation.getId())
                .participantType(ConversationParticipantType.USER)
                .participantId(String.valueOf(userId))
                .joinedAt(now)
                .build();

        final List<ConversationParticipant> agentParticipants = selectedAgents.stream()
                .map(agent -> ConversationParticipant.builder()
                        .id(UUID.randomUUID())
                        .conversationId(conversation.getId())
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId(agent.getId().toString())
                        .name(agent.getName())
                        .description(agent.getDescription())
                        .status(agent.getStatus())
                        .joinedAt(now)
                        .build())
                .toList();

        this.conversationParticipantRepository.saveAll(
                Stream.concat(Stream.of(userParticipant), agentParticipants.stream()).toList()
        );

        return ProjectConversationDetails.builder()
                .conversation(conversation)
                .project(project)
                .participants(agentParticipants)
                .build();
    }

    private String resolveTitle(final List<ProjectAgent> selectedAgents) {
        if (selectedAgents.size() == 1) {
            return "Chat with " + selectedAgents.get(0).getName();
        }
        return "Team chat";
    }
}
