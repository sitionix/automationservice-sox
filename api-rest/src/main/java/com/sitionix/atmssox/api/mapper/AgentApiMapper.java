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
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.CreateAgentProjectCommand;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {
        ChatExecutionStatusApiMapper.class,
        ChatExecutionFailureApiMapper.class
})
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    CreateAgentProjectCommand asCreateAgentProjectCommand(CreateAgentProjectRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    default CreateProjectConversationCommand asCreateProjectConversationCommand(final CreateProjectConversationRequestDTO src) {
        return CreateProjectConversationCommand.builder()
                .agentIds(src == null || src.getAgentIds() == null ? List.of() : List.copyOf(src.getAgentIds()))
                .build();
    }

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
                .items(details.stream().map(this::asProjectConversationDto).toList())
                .build();
    }

    default ProjectConversationDTO asProjectConversationDto(final ProjectConversationDetails details) {
        return ProjectConversationDTO.builder()
                .id(details.getConversation().getId())
                .projectId(details.getConversation().getProjectId())
                .type(ProjectConversationDTO.TypeEnum.fromValue(details.getConversation().getType().name()))
                .title(details.getConversation().getTitle())
                .status(ProjectConversationDTO.StatusEnum.fromValue(details.getConversation().getStatus().name()))
                .participants(this.asProjectConversationParticipants(details.getParticipants()))
                .canSendMessages(Boolean.FALSE)
                .createdAt(this.map(details.getConversation().getCreatedAt()))
                .updatedAt(this.map(details.getConversation().getUpdatedAt()))
                .lastMessageAt(this.map(details.getConversation().getLastMessageAt()))
                .build();
    }

    default ProjectConversationDetailsDTO asProjectConversationDetailsDto(final ProjectConversationDetails details) {
        return ProjectConversationDetailsDTO.builder()
                .id(details.getConversation().getId())
                .projectId(details.getConversation().getProjectId())
                .project(this.asProjectConversationProjectDto(details.getProject()))
                .type(ProjectConversationDetailsDTO.TypeEnum.fromValue(details.getConversation().getType().name()))
                .title(details.getConversation().getTitle())
                .status(ProjectConversationDetailsDTO.StatusEnum.fromValue(details.getConversation().getStatus().name()))
                .participants(this.asProjectConversationParticipants(details.getParticipants()))
                .messages(List.of())
                .canSendMessages(Boolean.FALSE)
                .createdAt(this.map(details.getConversation().getCreatedAt()))
                .updatedAt(this.map(details.getConversation().getUpdatedAt()))
                .lastMessageAt(this.map(details.getConversation().getLastMessageAt()))
                .build();
    }

    default ProjectConversationProjectDTO asProjectConversationProjectDto(final AgentProject project) {
        if (project == null) {
            return null;
        }
        return ProjectConversationProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .context(project.getContext())
                .build();
    }

    default List<ProjectConversationParticipantDTO> asProjectConversationParticipants(final List<ConversationParticipant> participants) {
        return participants.stream()
                .filter(participant -> participant.getParticipantType() == ConversationParticipantType.AGENT)
                .map(participant -> ProjectConversationParticipantDTO.builder()
                        .type(ProjectConversationParticipantDTO.TypeEnum.AGENT)
                        .agentId(UUID.fromString(participant.getParticipantId()))
                        .name(participant.getName())
                        .description(participant.getDescription())
                        .status(ProjectConversationParticipantDTO.StatusEnum.fromValue(participant.getStatus().name()))
                        .build())
                .toList();
    }
}
