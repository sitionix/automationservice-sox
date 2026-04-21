package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
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
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    ChatAgentResponseDTO asChatAgentResponseDto(ChatAgentResponse src);

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
        return AgentsResponseDTO.builder()
                .items(this.asAgentDtos(agents))
                .build();
    }

    default AgentConversationsResponseDTO asAgentConversationsResponseDto(final List<Conversation> conversations) {
        return AgentConversationsResponseDTO.builder()
                .items(this.asAgentConversationDtos(conversations))
                .build();
    }

    default AgentConversationDetailsDTO asAgentConversationDetailsDto(final ConversationDetails details) {
        final Conversation conversation = details.getConversation();
        return AgentConversationDetailsDTO.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .type(AgentConversationDetailsDTO.TypeEnum.fromValue(conversation.getType().name()))
                .createdAt(this.map(conversation.getCreatedAt()))
                .updatedAt(this.map(conversation.getUpdatedAt()))
                .lastMessageAt(this.map(conversation.getLastMessageAt()))
                .messages(this.asAgentConversationMessageDtos(details.getMessages()))
                .build();
    }
}
