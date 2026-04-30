package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO1;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {
        ChatExecutionStatusApiMapper.class,
        ChatExecutionFailureApiMapper.class
})
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    @Mapping(target = "assistantMessage", source = "reply")
    ChatAgentResponseDTO asChatAgentResponseDto(ChatAgentResponse src);

    @Mapping(target = "status", source = "status")
    SubmitChatExecutionResponseDTO asSubmitChatExecutionResponseDto(ChatExecution src);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "error", source = "failure")
    ChatExecutionDTO asChatExecutionDto(ChatExecution src);

    AgentConversationDTO1 asAgentConversationDto(Conversation src);

    AgentConversationMessageDTO asAgentConversationMessageDto(ConversationMessage src);

    AgentDTO asAgentDto(Agent src);

    List<AgentDTO> asAgentDtos(List<Agent> src);

    List<AgentConversationDTO1> asAgentConversationDtos(List<Conversation> src);

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
    AgentConversationDetailsDTO asAgentConversationDetailsDto(ConversationDetails details);
}
