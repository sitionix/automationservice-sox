package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentResponseDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationAuthorType;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AgentApiMapperTest {

    private AgentApiMapper agentApiMapper;

    @BeforeEach
    void setUp() {
        this.agentApiMapper = new AgentApiMapperImpl();
    }

    @Test
    void givenCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnCreateAgentCommand() {
        //given
        final CreateAgentRequestDTO given = this.getCreateAgentRequestDto();
        final CreateAgentCommand expected = this.getCreateAgentCommand();

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnNull() {
        //given
        final CreateAgentRequestDTO given = null;

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenPatchAgentRequestDto_whenAsPatchAgentCommand_thenReturnPatchAgentCommand() {
        //given
        final PatchAgentRequestDTO given = this.getPatchAgentRequestDto();
        final PatchAgentCommand expected = this.getPatchAgentCommand();

        //when
        final PatchAgentCommand actual = this.agentApiMapper.asPatchAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenChatAgentRequestDto_whenAsChatAgentCommand_thenReturnChatAgentCommand() {
        //given
        final ChatAgentRequestDTO given = ChatAgentRequestDTO.builder()
                .conversationId(UUID.fromString("21111111-1111-1111-1111-111111111111"))
                .message("Explain clean architecture")
                .build();
        final ChatAgentCommand expected = ChatAgentCommand.builder()
                .conversationId(UUID.fromString("21111111-1111-1111-1111-111111111111"))
                .message("Explain clean architecture")
                .build();

        //when
        final ChatAgentCommand actual = this.agentApiMapper.asChatAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullPatchAgentRequestDto_whenAsPatchAgentCommand_thenReturnNull() {
        //given
        final PatchAgentRequestDTO given = null;

        //when
        final PatchAgentCommand actual = this.agentApiMapper.asPatchAgentCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenCreateAgentRequestDtoWithNullDescription_whenAsCreateAgentCommand_thenReturnCommandWithNullDescription() {
        //given
        final CreateAgentRequestDTO given = CreateAgentRequestDTO.builder()
                .name("My agent")
                .description(null)
                .build();
        final CreateAgentCommand expected = CreateAgentCommand.builder()
                .name("My agent")
                .description(null)
                .build();

        //when
        final CreateAgentCommand actual = this.agentApiMapper.asCreateAgentCommand(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgent_whenAsAgentDto_thenReturnAgentDto() {
        //given
        final Agent given = this.getDomainAgent();
        final AgentDTO expected = this.getApiAgent();

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgent_whenAsAgentDto_thenReturnNull() {
        //given
        final Agent given = null;

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenAgentWithNullStatus_whenAsAgentDto_thenReturnAgentDtoWithNullStatus() {
        //given
        final Agent given = this.getDomainAgentWithNullStatus();
        final AgentDTO expected = this.getApiAgentWithNullStatus();

        //when
        final AgentDTO actual = this.agentApiMapper.asAgentDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAgentList_whenAsAgentsResponseDto_thenReturnAgentsResponseDto() {
        //given
        final List<Agent> given = List.of(this.getDomainAgent());
        final AgentsResponseDTO expected = this.getAgentsResponseDto();

        //when
        final AgentsResponseDTO actual = this.agentApiMapper.asAgentsResponseDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenConversations_whenAsAgentConversationsResponseDto_thenReturnAgentConversationsResponseDto() {
        //given
        final Conversation conversation = Conversation.builder()
                .id(UUID.fromString("31111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .title("Explain clean architecture")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:01:00Z"))
                .lastMessageAt(Instant.parse("2026-04-21T10:01:00Z"))
                .build();
        final AgentConversationDTO expectedItem = AgentConversationDTO.builder()
                .id(UUID.fromString("31111111-1111-1111-1111-111111111111"))
                .title("Explain clean architecture")
                .type(AgentConversationDTO.TypeEnum.DIRECT)
                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-04-21T10:01:00Z"))
                .lastMessageAt(OffsetDateTime.parse("2026-04-21T10:01:00Z"))
                .build();
        final AgentConversationsResponseDTO expected = AgentConversationsResponseDTO.builder()
                .items(List.of(expectedItem))
                .build();

        //when
        final AgentConversationsResponseDTO actual =
                this.agentApiMapper.asAgentConversationsResponseDto(List.of(conversation));

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenConversationDetails_whenAsAgentConversationDetailsDto_thenReturnAgentConversationDetailsDto() {
        //given
        final Conversation conversation = Conversation.builder()
                .id(UUID.fromString("41111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .title("Explain clean architecture")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .updatedAt(Instant.parse("2026-04-21T10:01:00Z"))
                .lastMessageAt(Instant.parse("2026-04-21T10:01:00Z"))
                .build();
        final ConversationMessage userMessage = ConversationMessage.builder()
                .id(UUID.fromString("51111111-1111-1111-1111-111111111111"))
                .conversationId(conversation.getId())
                .authorType(ConversationAuthorType.USER)
                .authorId("7")
                .content("Explain clean architecture")
                .createdAt(Instant.parse("2026-04-21T10:00:00Z"))
                .build();
        final ConversationMessage agentMessage = ConversationMessage.builder()
                .id(UUID.fromString("61111111-1111-1111-1111-111111111111"))
                .conversationId(conversation.getId())
                .authorType(ConversationAuthorType.AGENT)
                .authorId("agent-1")
                .content("Clean architecture separates domain from framework.")
                .createdAt(Instant.parse("2026-04-21T10:01:00Z"))
                .build();
        final ConversationDetails details = ConversationDetails.builder()
                .conversation(conversation)
                .messages(List.of(userMessage, agentMessage))
                .build();
        final AgentConversationDetailsDTO expected = AgentConversationDetailsDTO.builder()
                .id(UUID.fromString("41111111-1111-1111-1111-111111111111"))
                .title("Explain clean architecture")
                .type(AgentConversationDetailsDTO.TypeEnum.DIRECT)
                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2026-04-21T10:01:00Z"))
                .lastMessageAt(OffsetDateTime.parse("2026-04-21T10:01:00Z"))
                .messages(List.of(
                        AgentConversationMessageDTO.builder()
                                .id(UUID.fromString("51111111-1111-1111-1111-111111111111"))
                                .authorType(AgentConversationMessageDTO.AuthorTypeEnum.USER)
                                .authorId("7")
                                .content("Explain clean architecture")
                                .createdAt(OffsetDateTime.parse("2026-04-21T10:00:00Z"))
                                .build(),
                        AgentConversationMessageDTO.builder()
                                .id(UUID.fromString("61111111-1111-1111-1111-111111111111"))
                                .authorType(AgentConversationMessageDTO.AuthorTypeEnum.AGENT)
                                .authorId("agent-1")
                                .content("Clean architecture separates domain from framework.")
                                .createdAt(OffsetDateTime.parse("2026-04-21T10:01:00Z"))
                                .build()
                ))
                .build();

        //when
        final AgentConversationDetailsDTO actual = this.agentApiMapper.asAgentConversationDetailsDto(details);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenChatAgentResponse_whenAsChatAgentResponseDto_thenReturnChatAgentResponseDto() {
        //given
        final ConversationMessage reply = ConversationMessage.builder()
                .id(UUID.fromString("71111111-1111-1111-1111-111111111111"))
                .conversationId(UUID.fromString("81111111-1111-1111-1111-111111111111"))
                .authorType(ConversationAuthorType.AGENT)
                .authorId("agent-1")
                .content("A simple example is...")
                .createdAt(Instant.parse("2026-04-21T10:03:00Z"))
                .build();
        final ChatAgentResponse given = ChatAgentResponse.builder()
                .conversationId(UUID.fromString("81111111-1111-1111-1111-111111111111"))
                .reply(reply)
                .build();
        final ChatAgentResponseDTO expected = ChatAgentResponseDTO.builder()
                .conversationId(UUID.fromString("81111111-1111-1111-1111-111111111111"))
                .reply(AgentConversationMessageDTO.builder()
                        .id(UUID.fromString("71111111-1111-1111-1111-111111111111"))
                        .authorType(AgentConversationMessageDTO.AuthorTypeEnum.AGENT)
                        .authorId("agent-1")
                        .content("A simple example is...")
                        .createdAt(OffsetDateTime.parse("2026-04-21T10:03:00Z"))
                        .build())
                .build();

        //when
        final ChatAgentResponseDTO actual = this.agentApiMapper.asChatAgentResponseDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNullAgentList_whenAsAgentDtos_thenReturnNull() {
        //given
        final List<Agent> given = null;

        //when
        final List<AgentDTO> actual = this.agentApiMapper.asAgentDtos(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenNullInstant_whenMap_thenReturnNull() {
        //given
        final Instant given = null;

        //when
        final OffsetDateTime actual = this.agentApiMapper.map(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenInstant_whenMap_thenReturnOffsetDateTimeInUtc() {
        //given
        final Instant given = Instant.parse("2026-01-15T12:13:14Z");

        //when
        final OffsetDateTime actual = this.agentApiMapper.map(given);

        //then
        assertThat(actual).isEqualTo(OffsetDateTime.parse("2026-01-15T12:13:14Z"));
    }

    private CreateAgentRequestDTO getCreateAgentRequestDto() {
        return CreateAgentRequestDTO.builder()
                .name("My agent")
                .description("My description")
                .build();
    }

    private CreateAgentCommand getCreateAgentCommand() {
        return CreateAgentCommand.builder()
                .name("My agent")
                .description("My description")
                .build();
    }

    private PatchAgentRequestDTO getPatchAgentRequestDto() {
        return PatchAgentRequestDTO.builder()
                .name("My patched agent")
                .description("My patched description")
                .instruction("My patched instruction")
                .build();
    }

    private PatchAgentCommand getPatchAgentCommand() {
        return PatchAgentCommand.builder()
                .name("My patched agent")
                .description("My patched description")
                .instruction("My patched instruction")
                .build();
    }

    private Agent getDomainAgent() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(AgentStatus.DRAFT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private AgentDTO getApiAgent() {
        return AgentDTO.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(AgentDTO.StatusEnum.DRAFT)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private Agent getDomainAgentWithNullStatus() {
        final Instant createdAt = Instant.parse("2026-01-10T10:15:30Z");
        final Instant updatedAt = Instant.parse("2026-01-10T10:20:30Z");
        return Agent.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .userId(7L)
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(null)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private AgentDTO getApiAgentWithNullStatus() {
        return AgentDTO.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(null)
                .createdAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:15:30Z"), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(Instant.parse("2026-01-10T10:20:30Z"), ZoneOffset.UTC))
                .build();
    }

    private AgentsResponseDTO getAgentsResponseDto() {
        return AgentsResponseDTO.builder()
                .items(List.of(this.getApiAgent()))
                .build();
    }
}
