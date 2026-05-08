package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentProjectRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateProjectConversationRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationParticipantDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationProjectDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.mapstruct.BeanMapping;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {
        ChatExecutionStatusApiMapper.class,
        ChatExecutionFailureApiMapper.class
})
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    CreateAgentProjectCommand asCreateAgentProjectCommand(CreateAgentProjectRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    @Mapping(target = "agentIds", source = "src", qualifiedByName = "extractAgentIds")
    CreateProjectConversationCommand asCreateProjectConversationCommand(CreateProjectConversationRequestDTO src);

    @Mapping(target = "assistantMessage", source = "reply")
    ChatAgentExecutionDTO asChatAgentResponseDto(ChatAgentResponse src);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "inputMessageId", source = "inputMessageId")
    SubmitChatExecutionResponseDTO asSubmitChatExecutionResponseDto(ChatExecution src);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "error", source = "failure")
    ChatExecutionDTO asChatExecutionDto(ChatExecution src);

    AgentConversationDTO asAgentConversationDto(Conversation src);

    AgentConversationMessageDTO asAgentConversationMessageDto(ConversationMessage src);

    AgentDTO asAgentDto(Agent src);

    List<AgentDTO> asAgentDtos(List<Agent> src);

    List<AgentConversationDTO> asAgentConversationDtos(List<Conversation> src);

    List<AgentConversationMessageDTO> asAgentConversationMessageDtos(List<ConversationMessage> src);

    default OffsetDateTime map(final Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default AgentsResponseDTO asAgentsResponseDto(final List<Agent> agents) {
        return new AgentsResponseDTO()
                .items(this.asAgentDtos(agents));
    }

    default AgentConversationsResponseDTO asAgentConversationsResponseDto(final List<Conversation> conversations) {
        return new AgentConversationsResponseDTO()
                .items(this.asAgentConversationDtos(conversations));
    }

    @Mapping(target = "id", source = "conversation.id")
    @Mapping(target = "title", source = "conversation.title")
    @Mapping(target = "type", source = "conversation.type")
    @Mapping(target = "createdAt", source = "conversation.createdAt")
    @Mapping(target = "updatedAt", source = "conversation.updatedAt")
    @Mapping(target = "lastMessageAt", source = "conversation.lastMessageAt")
    @Mapping(target = "messages", source = "messages")
    @Mapping(target = "executions", source = "executions")
    AgentConversationDetailsDTO asAgentConversationDetailsDto(ConversationDetails details);

    default ProjectConversationsResponseDTO asProjectConversationsResponseDto(final List<ProjectConversationDetails> details) {
        return ProjectConversationsResponseDTO.builder()
                .items(this.asProjectConversationDtos(details))
                .build();
    }

    List<ProjectConversationDTO> asProjectConversationDtos(List<ProjectConversationDetails> details);

    @Mapping(target = "id", source = "conversation.id")
    @Mapping(target = "projectId", source = "conversation.projectId")
    @Mapping(target = "type", source = "conversation.type")
    @Mapping(target = "title", source = "conversation.title")
    @Mapping(target = "status", source = "conversation.status")
    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapAgentParticipants")
    @Mapping(target = "canSendMessages", constant = "false")
    @Mapping(target = "createdAt", source = "conversation.createdAt")
    @Mapping(target = "updatedAt", source = "conversation.updatedAt")
    @Mapping(target = "lastMessageAt", source = "conversation.lastMessageAt")
    ProjectConversationDTO asProjectConversationDto(ProjectConversationDetails details);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "id", source = "conversation.id")
    @Mapping(target = "projectId", source = "conversation.projectId")
    @Mapping(target = "project", source = "project")
    @Mapping(target = "type", source = "conversation.type")
    @Mapping(target = "title", source = "conversation.title")
    @Mapping(target = "status", source = "conversation.status")
    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapAgentParticipants")
    @Mapping(target = "messages", source = "details", qualifiedByName = "mapProjectConversationMessages")
    @Mapping(target = "canSendMessages", constant = "false")
    @Mapping(target = "createdAt", source = "conversation.createdAt")
    @Mapping(target = "updatedAt", source = "conversation.updatedAt")
    @Mapping(target = "lastMessageAt", source = "conversation.lastMessageAt")
    ProjectConversationDetailsDTO asProjectConversationDetailsDto(ProjectConversationDetails details);

    ProjectConversationProjectDTO asProjectConversationProjectDto(AgentProject project);

    @Named("extractAgentIds")
    default List<UUID> extractAgentIds(final CreateProjectConversationRequestDTO source) {
        if (source == null || source.getAgentIds() == null) {
            return List.of();
        }
        return List.copyOf(source.getAgentIds());
    }

    default UUID map(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    @Mapping(target = "type", constant = "AGENT")
    @Mapping(target = "agentId", source = "participantId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    ProjectConversationParticipantDTO asProjectConversationParticipantDto(ConversationParticipant src);

    @Named("mapAgentParticipants")
    default List<ProjectConversationParticipantDTO> mapAgentParticipants(final List<ConversationParticipant> source) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .filter(participant -> ConversationParticipantType.AGENT.equals(participant.getParticipantType()))
                .map(this::asProjectConversationParticipantDto)
                .toList();
    }

    @Named("mapProjectConversationMessages")
    default List<AgentConversationMessageDTO> mapProjectConversationMessages(final ProjectConversationDetails source) {
        return List.of();
    }


    default ProjectConversationDTO.TypeEnum mapProjectConversationType(final ConversationType type) {
        return type == null ? null : ProjectConversationDTO.TypeEnum.fromValue(type.name());
    }

    default ProjectConversationDTO.StatusEnum mapProjectConversationStatus(final ConversationStatus status) {
        return status == null ? null : ProjectConversationDTO.StatusEnum.fromValue(status.name());
    }

    default ProjectConversationDetailsDTO.TypeEnum mapProjectConversationDetailsType(final ConversationType type) {
        return type == null ? null : ProjectConversationDetailsDTO.TypeEnum.fromValue(type.name());
    }

    default ProjectConversationDetailsDTO.StatusEnum mapProjectConversationDetailsStatus(final ConversationStatus status) {
        return status == null ? null : ProjectConversationDetailsDTO.StatusEnum.fromValue(status.name());
    }

    default ProjectConversationParticipantDTO.StatusEnum mapProjectConversationParticipantStatus(final AgentStatus status) {
        return status == null ? null : ProjectConversationParticipantDTO.StatusEnum.fromValue(status.name());
    }
}
