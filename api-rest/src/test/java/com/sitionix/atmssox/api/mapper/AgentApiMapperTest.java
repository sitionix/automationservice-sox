package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.AgentDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationMessageDTO;
import com.app_afesox.atmssox.api_first.dto.AgentConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.AgentsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ChatAgentExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionDTO;
import com.app_afesox.atmssox.api_first.dto.ChatExecutionFailureDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ExecutionStatusDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationDetails;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentApiMapperTest {

    private AgentApiMapper agentApiMapper;

    @Mock
    private ChatExecutionStatusApiMapper chatExecutionStatusApiMapper;

    @Mock
    private ChatExecutionFailureApiMapper chatExecutionFailureApiMapper;

    @BeforeEach
    void setUp() {
        this.agentApiMapper = new AgentApiMapperImpl(
                this.chatExecutionStatusApiMapper,
                this.chatExecutionFailureApiMapper
        );
    }

    @Test
    void givenCreateAgentRequestDto_whenAsCreateAgentCommand_thenReturnCreateAgentCommand() {
        //given
        final CreateAgentRequestDTO given = this.getCreateAgentRequestDto("My description");
        final CreateAgentCommand expected = this.getCreateAgentCommand("My description");

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
        final UUID conversationId = UUID.fromString("21111111-1111-1111-1111-111111111111");
        final ChatAgentRequestDTO given = this.getChatAgentRequestDto(conversationId, "Explain clean architecture");
        final ChatAgentCommand expected = this.getChatAgentCommand(conversationId, "Explain clean architecture");

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
        final CreateAgentRequestDTO given = this.getCreateAgentRequestDto(null);
        final CreateAgentCommand expected = this.getCreateAgentCommand(null);

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
        final Conversation conversation = this.getConversation(
                UUID.fromString("31111111-1111-1111-1111-111111111111"),
                "Explain clean architecture",
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:01:00Z")
        );
        final AgentConversationsResponseDTO expected = this.getAgentConversationsResponseDto(
                UUID.fromString("31111111-1111-1111-1111-111111111111"),
                "Explain clean architecture",
                OffsetDateTime.parse("2026-04-21T10:00:00Z"),
                OffsetDateTime.parse("2026-04-21T10:01:00Z")
        );

        //when
        final AgentConversationsResponseDTO actual =
                this.agentApiMapper.asAgentConversationsResponseDto(List.of(conversation));

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenConversationDetails_whenAsAgentConversationDetailsDto_thenReturnAgentConversationDetailsDto() {
        //given
        final UUID conversationId = UUID.fromString("41111111-1111-1111-1111-111111111111");
        final ConversationDetails details = this.getConversationDetails(conversationId);
        final AgentConversationDetailsDTO expected = this.getAgentConversationDetailsDto(conversationId);

        //when
        final AgentConversationDetailsDTO actual = this.agentApiMapper.asAgentConversationDetailsDto(details);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenChatAgentResponse_whenAsChatAgentResponseDto_thenReturnChatAgentResponseDto() {
        //given
        final UUID conversationId = UUID.fromString("81111111-1111-1111-1111-111111111111");
        final ChatAgentResponse given = this.getChatAgentResponse(conversationId);
        final ChatAgentExecutionDTO expected = this.getChatAgentResponseDto(conversationId);

        //when
        final ChatAgentExecutionDTO actual = this.agentApiMapper.asChatAgentResponseDto(given);

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

    @Test
    void givenChatExecution_whenAsSubmitChatExecutionResponseDto_thenReturnExecutionEnvelope() {
        //given
        final ChatExecution given = this.getQueuedChatExecution();
        when(this.chatExecutionStatusApiMapper.map(ChatExecutionStatus.QUEUED)).thenReturn(ExecutionStatusDTO.ACCEPTED);

        //when
        final SubmitChatExecutionResponseDTO actual = this.agentApiMapper.asSubmitChatExecutionResponseDto(given);

        //then
        assertThat(actual.getExecutionId()).isEqualTo(given.getExecutionId());
        assertThat(actual.getConversationId()).isEqualTo(given.getConversationId());
        assertThat(actual.getStatus()).isEqualTo(ExecutionStatusDTO.ACCEPTED);
    }

    @Test
    void givenChatExecutionWithFailure_whenAsChatExecutionDto_thenReturnMappedExecutionAndFailure() {
        //given
        final ChatExecution given = this.getFailedChatExecution();
        when(this.chatExecutionStatusApiMapper.map(ChatExecutionStatus.FAILED)).thenReturn(ExecutionStatusDTO.FAILED);
        when(this.chatExecutionFailureApiMapper.asChatExecutionFailureDto(given.getFailure())).thenReturn(
                ChatExecutionFailureDTO.builder()
                        .code("EXECUTION_ERROR")
                        .message("Execution failed")
                        .details(Map.of("retryable", true))
                        .build()
        );

        //when
        final ChatExecutionDTO actual = this.agentApiMapper.asChatExecutionDto(given);

        //then
        assertThat(actual.getStatus()).isEqualTo(ExecutionStatusDTO.FAILED);
        assertThat(actual.getError()).isEqualTo(ChatExecutionFailureDTO.builder()
                .code("EXECUTION_ERROR")
                .message("Execution failed")
                .details(Map.of("retryable", true))
                .build());
    }

    private CreateAgentRequestDTO getCreateAgentRequestDto(final String description) {
        return CreateAgentRequestDTO.builder()
                .name("My agent")
                .description(description)
                .build();
    }

    private CreateAgentCommand getCreateAgentCommand(final String description) {
        return CreateAgentCommand.builder()
                .name("My agent")
                .description(description)
                .build();
    }

    private ChatAgentRequestDTO getChatAgentRequestDto(final UUID conversationId, final String message) {
        return ChatAgentRequestDTO.builder()
                .conversationId(conversationId)
                .message(message)
                .build();
    }

    private ChatAgentCommand getChatAgentCommand(final UUID conversationId, final String message) {
        return ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message(message)
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

    private Conversation getConversation(
            final UUID id,
            final String title,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        return Conversation.builder()
                .id(id)
                .userId(7L)
                .title(title)
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastMessageAt(updatedAt)
                .build();
    }

    private AgentConversationsResponseDTO getAgentConversationsResponseDto(
            final UUID id,
            final String title,
            final OffsetDateTime createdAt,
            final OffsetDateTime updatedAt
    ) {
        return AgentConversationsResponseDTO.builder()
                .items(List.of(AgentConversationDTO.builder()
                        .id(id)
                        .title(title)
                        .type(AgentConversationDTO.TypeEnum.DIRECT)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .lastMessageAt(updatedAt)
                        .build()))
                .build();
    }

    private ConversationDetails getConversationDetails(final UUID conversationId) {
        final Conversation conversation = this.getConversation(
                conversationId,
                "Explain clean architecture",
                Instant.parse("2026-04-21T10:00:00Z"),
                Instant.parse("2026-04-21T10:01:00Z")
        );
        final ConversationMessage userMessage = this.getConversationMessage(
                UUID.fromString("51111111-1111-1111-1111-111111111111"),
                conversationId,
                ConversationParticipantType.USER,
                "7",
                "Explain clean architecture",
                Instant.parse("2026-04-21T10:00:00Z")
        );
        final ConversationMessage agentMessage = this.getConversationMessage(
                UUID.fromString("61111111-1111-1111-1111-111111111111"),
                conversationId,
                ConversationParticipantType.AGENT,
                "agent-1",
                "Clean architecture separates domain from framework.",
                Instant.parse("2026-04-21T10:01:00Z")
        );
        return ConversationDetails.builder()
                .conversation(conversation)
                .messages(List.of(userMessage, agentMessage))
                .build();
    }

    private AgentConversationDetailsDTO getAgentConversationDetailsDto(final UUID conversationId) {
        return AgentConversationDetailsDTO.builder()
                .id(conversationId)
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
    }

    private ChatAgentResponse getChatAgentResponse(final UUID conversationId) {
        final ConversationMessage reply = this.getConversationMessage(
                UUID.fromString("71111111-1111-1111-1111-111111111111"),
                conversationId,
                ConversationParticipantType.AGENT,
                "agent-1",
                "A simple example is...",
                Instant.parse("2026-04-21T10:03:00Z")
        );
        return ChatAgentResponse.builder()
                .conversationId(conversationId)
                .reply(reply)
                .build();
    }

    private ChatAgentExecutionDTO getChatAgentResponseDto(final UUID conversationId) {
        return ChatAgentExecutionDTO.builder()
                .conversationId(conversationId)
                .assistantMessage(AgentConversationMessageDTO.builder()
                        .id(UUID.fromString("71111111-1111-1111-1111-111111111111"))
                        .authorType(AgentConversationMessageDTO.AuthorTypeEnum.AGENT)
                        .authorId("agent-1")
                        .content("A simple example is...")
                        .createdAt(OffsetDateTime.parse("2026-04-21T10:03:00Z"))
                        .build())
                .build();
    }

    private ConversationMessage getConversationMessage(
            final UUID id,
            final UUID conversationId,
            final ConversationParticipantType authorType,
            final String authorId,
            final String content,
            final Instant createdAt
    ) {
        return ConversationMessage.builder()
                .id(id)
                .conversationId(conversationId)
                .authorType(authorType)
                .authorId(authorId)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

    private ChatExecution getQueuedChatExecution() {
        return ChatExecution.builder()
                .executionId(UUID.fromString("d8827667-03f3-4d46-ae0d-d35e43ecdf95"))
                .conversationId(UUID.fromString("5bddb194-5ca2-4461-9b6b-c5f986fa86ea"))
                .status(ChatExecutionStatus.QUEUED)
                .createdAt(Instant.parse("2026-04-29T10:00:00Z"))
                .build();
    }

    private ChatExecution getFailedChatExecution() {
        return ChatExecution.builder()
                .executionId(UUID.fromString("d8827667-03f3-4d46-ae0d-d35e43ecdf95"))
                .conversationId(UUID.fromString("5bddb194-5ca2-4461-9b6b-c5f986fa86ea"))
                .agentId(UUID.fromString("6e4e32f8-2f48-4600-9a73-bb026f98dbf4"))
                .status(ChatExecutionStatus.FAILED)
                .failure(this.getExecutionFailure())
                .createdAt(Instant.parse("2026-04-29T10:00:00Z"))
                .build();
    }

    private ChatExecutionFailure getExecutionFailure() {
        return ChatExecutionFailure.builder()
                .failureClass(ChatExecutionFailureClass.EXECUTION_ERROR)
                .reason("Execution failed")
                .retryable(true)
                .build();
    }

}
