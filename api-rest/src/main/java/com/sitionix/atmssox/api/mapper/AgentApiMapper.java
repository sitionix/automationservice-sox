package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRuleDTO;
import com.app_afesox.atmssox.api_first.dto.AgentRulesResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.DeleteAgentRuleResponseDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRuleRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentApiMapper {

    CreateAgentCommand asCreateAgentCommand(CreateAgentRequestDTO src);

    CreateAgentRuleCommand asCreateAgentRuleCommand(CreateAgentRuleRequestDTO src);

    PatchAgentCommand asPatchAgentCommand(PatchAgentRequestDTO src);

    PatchAgentRuleCommand asPatchAgentRuleCommand(PatchAgentRuleRequestDTO src);

    ChatAgentCommand asChatAgentCommand(ChatAgentRequestDTO src);

    ChatAgentResponseDTO asChatAgentResponseDto(ChatAgentResponse src);

    AgentConversationDTO asAgentConversationDto(Conversation src);

    AgentConversationMessageDTO asAgentConversationMessageDto(ConversationMessage src);

    AgentDTO asAgentDto(Agent src);

    AgentRuleDTO asAgentRuleDto(AgentRule src);

    List<AgentDTO> asAgentDtos(List<Agent> src);

    List<AgentRuleDTO> asAgentRuleDtos(List<AgentRule> src);

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

    default AgentRulesResponseDTO asAgentRulesResponseDto(final List<AgentRule> rules) {
        return new AgentRulesResponseDTO()
                .items(this.asAgentRuleDtos(rules));
    }

    default DeleteAgentRuleResponseDTO asDeleteAgentRuleResponseDto(final DeleteAgentRuleResponse src) {
        return new DeleteAgentRuleResponseDTO()
                .status(DeleteAgentRuleResponseDTO.StatusEnum.DELETED);
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
