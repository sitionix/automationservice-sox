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
import com.app_afesox.atmssox.api_first.dto.CreateProjectConversationRequestDTO;
import com.app_afesox.atmssox.api_first.dto.CreateAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ExecutionStatusDTO;
import com.app_afesox.atmssox.api_first.dto.PatchAgentRequestDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationDetailsDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationParticipantDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationProjectDTO;
import com.app_afesox.atmssox.api_first.dto.ProjectConversationsResponseDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitChatExecutionResponseDTO;
import com.app_afesox.atmssox.api_first.dto.SubmitConversationExecutionResponseDTO;
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
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.CreateAgentCommand;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.PatchAgentCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.model.AgentProject;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentApiMapperTest {

    private AgentApiMapper agentApiMapper;

    @Mock private ChatExecutionStatusApiMapper chatExecutionStatusApiMapper;
    @Mock private ChatExecutionFailureApiMapper chatExecutionFailureApiMapper;

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
        assertThat(actual.getInputMessageId()).isEqualTo(given.getInputMessageId());
        assertThat(actual.getStatus()).isEqualTo(ExecutionStatusDTO.ACCEPTED);
    }

    @Test
    void givenChatExecution_whenAsSubmitConversationExecutionResponseDto_thenReturnExecutionEnvelope() {
        //given
        final ChatExecution given = this.getQueuedChatExecution();
        when(this.chatExecutionStatusApiMapper.map(ChatExecutionStatus.QUEUED)).thenReturn(ExecutionStatusDTO.ACCEPTED);

        //when
        final SubmitConversationExecutionResponseDTO actual = this.agentApiMapper.asSubmitConversationExecutionResponseDto(given);

        //then
        assertThat(actual.getExecutionId()).isEqualTo(given.getExecutionId());
        assertThat(actual.getConversationId()).isEqualTo(given.getConversationId());
        assertThat(actual.getInputMessageId()).isEqualTo(given.getInputMessageId());
        assertThat(actual.getRuntimeDispatched()).isTrue();
        assertThat(actual.getExecutionStatus()).isEqualTo(ExecutionStatusDTO.ACCEPTED);
    }

    @Test
    void givenChatExecutionWithoutStatus_whenAsSubmitConversationExecutionResponseDto_thenRuntimeNotDispatchedAndExecutionStatusNull() {
        //given
        final ChatExecution given = this.getQueuedChatExecution().toBuilder()
                .status(null)
                .build();

        //when
        final SubmitConversationExecutionResponseDTO actual = this.agentApiMapper.asSubmitConversationExecutionResponseDto(given);

        //then
        assertThat(actual.getExecutionId()).isEqualTo(given.getExecutionId());
        assertThat(actual.getConversationId()).isEqualTo(given.getConversationId());
        assertThat(actual.getInputMessageId()).isEqualTo(given.getInputMessageId());
        assertThat(actual.getRuntimeDispatched()).isFalse();
        assertThat(actual.getExecutionStatus()).isNull();
        verify(this.chatExecutionStatusApiMapper).map(null);
    }

    @Test
    void givenChatExecutionWithoutStatus_whenAsSubmitConversationExecutionResponseDto_thenReturnNotDispatchedResponse() {
        //given
        final ChatExecution given = this.getSubmitConversationExecutionWithoutRuntimeDispatch();

        //when
        final SubmitConversationExecutionResponseDTO actual = this.agentApiMapper.asSubmitConversationExecutionResponseDto(given);

        //then
        assertThat(actual.getExecutionId()).isNull();
        assertThat(actual.getConversationId()).isEqualTo(given.getConversationId());
        assertThat(actual.getInputMessageId()).isEqualTo(given.getInputMessageId());
        assertThat(actual.getRuntimeDispatched()).isFalse();
        assertThat(actual.getExecutionStatus()).isNull();
        verify(this.chatExecutionStatusApiMapper).map(null);
    }

    @Test
    void givenNullChatExecution_whenAsSubmitConversationExecutionResponseDto_thenReturnNull() {
        //given
        final ChatExecution given = null;

        //when
        final SubmitConversationExecutionResponseDTO actual = this.agentApiMapper.asSubmitConversationExecutionResponseDto(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenChatExecutionWithFailure_whenAsChatExecutionDto_thenReturnMappedExecutionAndFailure() {
        //given
        final ChatExecution given = this.getFailedChatExecution();
        final ChatExecutionFailureDTO chatExecutionFailureDto = this.getChatExecutionFailureDto();
        when(this.chatExecutionStatusApiMapper.map(ChatExecutionStatus.FAILED)).thenReturn(ExecutionStatusDTO.FAILED);
        when(this.chatExecutionFailureApiMapper.asChatExecutionFailureDto(given.getFailure())).thenReturn(chatExecutionFailureDto);

        //when
        final ChatExecutionDTO actual = this.agentApiMapper.asChatExecutionDto(given);

        //then
        assertThat(actual.getStatus()).isEqualTo(ExecutionStatusDTO.FAILED);
        assertThat(actual.getError()).isEqualTo(chatExecutionFailureDto);
    }

    @Test
    void givenCreateProjectConversationRequestDto_whenAsCreateProjectConversationCommand_thenReturnMappedCommand() {
        //given
        final UUID agentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final CreateProjectConversationRequestDTO given = CreateProjectConversationRequestDTO.builder()
                .agentIds(Set.of(agentId))
                .build();

        //when
        final CreateProjectConversationCommand actual = this.agentApiMapper.asCreateProjectConversationCommand(given);

        //then
        assertThat(actual.getAgentIds()).isEqualTo(List.of(agentId));
    }

    @Test
    void givenNullCreateProjectConversationRequestDto_whenAsCreateProjectConversationCommand_thenReturnNull() {
        //given
        final CreateProjectConversationRequestDTO given = null;

        //when
        final CreateProjectConversationCommand actual = this.agentApiMapper.asCreateProjectConversationCommand(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenProjectConversationDetails_whenAsProjectConversationsResponseDto_thenReturnMappedItems() {
        //given
        final List<ProjectConversationDetails> details = List.of(this.getProjectConversationDetails());
        final ProjectConversationsResponseDTO expected = this.getProjectConversationsResponseDto();

        //when
        final ProjectConversationsResponseDTO actual = this.agentApiMapper.asProjectConversationsResponseDto(details);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenProjectConversationDetails_whenAsProjectConversationDetailsDto_thenReturnMappedDto() {
        //given
        final ProjectConversationDetails given = this.getProjectConversationDetails();
        final ProjectConversationDetailsDTO expected = this.getProjectConversationDetailsDto();

        //when
        final ProjectConversationDetailsDTO actual = this.agentApiMapper.asProjectConversationDetailsDto(given);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenProjectConversationDetailsWithUserAndAgentParticipants_whenAsProjectConversationDetailsDto_thenReturnOnlyAgentParticipants() {
        //given
        final ProjectConversationDetails given = this.getProjectConversationDetailsWithUserAndAgentParticipants();

        //when
        final ProjectConversationDetailsDTO actual = this.agentApiMapper.asProjectConversationDetailsDto(given);

        //then
        assertThat(actual.getParticipants())
                .extracting(ProjectConversationParticipantDTO::getType)
                .containsOnly(ProjectConversationParticipantDTO.TypeEnum.AGENT);
        assertThat(actual.getParticipants()).hasSize(1);
    }

    @Test
    void givenProjectConversationDetailsWithNullParticipants_whenAsProjectConversationDto_thenReturnEmptyParticipants() {
        //given
        final ProjectConversationDetails given = this.getProjectConversationDetailsWithNullParticipants();

        //when
        final ProjectConversationDTO actual = this.agentApiMapper.asProjectConversationDto(given);

        //then
        assertThat(actual.getParticipants()).isEqualTo(List.of());
        assertThat(actual.getCanSendMessages()).isFalse();
    }

    @Test
    void givenProjectConversationDetailsWithNullParticipants_whenAsProjectConversationDetailsDto_thenReturnEmptyParticipantsAndEmptyMessages() {
        //given
        final ProjectConversationDetails given = this.getProjectConversationDetailsWithNullParticipants();

        //when
        final ProjectConversationDetailsDTO actual = this.agentApiMapper.asProjectConversationDetailsDto(given);

        //then
        assertThat(actual.getParticipants()).isEqualTo(List.of());
        assertThat(actual.getMessages()).isEqualTo(List.of());
        assertThat(actual.getCanSendMessages()).isFalse();
    }

    @Test
    void givenConversationTypeAndStatusNull_whenMapProjectConversationEnums_thenReturnNull() {
        //given
        final ConversationType givenType = null;
        final ConversationStatus givenStatus = null;

        //when
        final ProjectConversationDTO.TypeEnum actualType = this.agentApiMapper.mapProjectConversationType(givenType);
        final ProjectConversationDTO.StatusEnum actualStatus = this.agentApiMapper.mapProjectConversationStatus(givenStatus);
        final ProjectConversationDetailsDTO.TypeEnum actualDetailsType = this.agentApiMapper.mapProjectConversationDetailsType(givenType);
        final ProjectConversationDetailsDTO.StatusEnum actualDetailsStatus = this.agentApiMapper.mapProjectConversationDetailsStatus(givenStatus);

        //then
        assertThat(actualType).isNull();
        assertThat(actualStatus).isNull();
        assertThat(actualDetailsType).isNull();
        assertThat(actualDetailsStatus).isNull();
    }

    @Test
    void givenAgentStatusNull_whenMapProjectConversationParticipantStatus_thenReturnNull() {
        //given
        final AgentStatus given = null;

        //when
        final ProjectConversationParticipantDTO.StatusEnum actual = this.agentApiMapper.mapProjectConversationParticipantStatus(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenDirectTypeAndDeletedStatus_whenMapProjectConversationEnums_thenReturnMappedEnums() {
        //given
        final ConversationType givenType = ConversationType.DIRECT;
        final ConversationStatus givenStatus = ConversationStatus.DELETED;
        final AgentStatus givenAgentStatus = AgentStatus.ARCHIVED;

        //when
        final ProjectConversationDTO.TypeEnum actualType = this.agentApiMapper.mapProjectConversationType(givenType);
        final ProjectConversationDTO.StatusEnum actualStatus = this.agentApiMapper.mapProjectConversationStatus(givenStatus);
        final ProjectConversationDetailsDTO.TypeEnum actualDetailsType = this.agentApiMapper.mapProjectConversationDetailsType(givenType);
        final ProjectConversationDetailsDTO.StatusEnum actualDetailsStatus = this.agentApiMapper.mapProjectConversationDetailsStatus(givenStatus);
        final ProjectConversationParticipantDTO.StatusEnum actualParticipantStatus =
                this.agentApiMapper.mapProjectConversationParticipantStatus(givenAgentStatus);

        //then
        assertThat(actualType).isEqualTo(ProjectConversationDTO.TypeEnum.DIRECT);
        assertThat(actualStatus).isEqualTo(ProjectConversationDTO.StatusEnum.DELETED);
        assertThat(actualDetailsType).isEqualTo(ProjectConversationDetailsDTO.TypeEnum.DIRECT);
        assertThat(actualDetailsStatus).isEqualTo(ProjectConversationDetailsDTO.StatusEnum.DELETED);
        assertThat(actualParticipantStatus).isEqualTo(ProjectConversationParticipantDTO.StatusEnum.ARCHIVED);
    }

    private ChatExecutionFailureDTO getChatExecutionFailureDto() {
        return ChatExecutionFailureDTO.builder()
                .code("EXECUTION_ERROR")
                .message("Execution failed")
                .details(Map.of("retryable", true))
                .build();
    }

    private ProjectConversationDetails getProjectConversationDetails() {
        return this.getProjectConversationDetails(
                this::getDefaultProjectConversation,
                List.of(this.getAgentConversationParticipant()),
                true
        );
    }

    private ProjectConversationDetails getProjectConversationDetailsWithUserAndAgentParticipants() {
        return this.getProjectConversationDetails(
                this::getDefaultProjectConversation,
                List.of(this.getUserConversationParticipant(), this.getAgentConversationParticipant()),
                true
        );
    }

    private ProjectConversationDetails getProjectConversationDetailsWithNullParticipants() {
        return this.getProjectConversationDetails(
                this::getDefaultProjectConversation,
                null,
                true
        );
    }

    private ProjectConversationDetails getProjectConversationDetails(
            final Supplier<Conversation> conversationBuilder,
            final List<ConversationParticipant> participants,
            final boolean includeProject
    ) {
        final AgentProject project = this.getProjectConversationProject();
        return ProjectConversationDetails.builder()
                .conversation(conversationBuilder.get())
                .project(includeProject ? project : null)
                .participants(participants)
                .build();
    }

    private Conversation getDefaultProjectConversation() {
        return this.getProjectConversation(this.getProjectConversationProject().getId(), "Team chat");
    }

    private AgentProject getProjectConversationProject() {
        return AgentProject.builder()
                .id(this.getProjectId())
                .name(this.getProjectName())
                .context(this.getProjectContext())
                .build();
    }

    private Conversation getProjectConversation(final UUID projectId, final String title) {
        return Conversation.builder()
                .id(this.getProjectConversationId())
                .projectId(projectId)
                .type(ConversationType.MULTI_AGENT)
                .title(title)
                .status(ConversationStatus.ACTIVE)
                .createdAt(this.getProjectCreatedAt())
                .updatedAt(this.getProjectUpdatedAt())
                .lastMessageAt(null)
                .build();
    }

    private ConversationParticipant getAgentConversationParticipant() {
        return this.getConversationParticipant(
                ConversationParticipantType.AGENT,
                this.getParticipantAgentId().toString(),
                this.getParticipantName(),
                this.getParticipantDescription(),
                AgentStatus.ACTIVE
        );
    }

    private ConversationParticipant getUserConversationParticipant() {
        return this.getConversationParticipant(
                ConversationParticipantType.USER,
                "7",
                "Owner",
                "Project owner",
                AgentStatus.ACTIVE
        );
    }

    private ConversationParticipant getConversationParticipant(
            final ConversationParticipantType participantType,
            final String participantId,
            final String name,
            final String description,
            final AgentStatus status
    ) {
        return ConversationParticipant.builder()
                .participantType(participantType)
                .participantId(participantId)
                .name(name)
                .description(description)
                .status(status)
                .build();
    }

    private ProjectConversationsResponseDTO getProjectConversationsResponseDto() {
        return ProjectConversationsResponseDTO.builder()
                .items(List.of(this.getProjectConversationDto()))
                .build();
    }

    private ProjectConversationDTO getProjectConversationDto() {
        return this.getProjectConversationDto(
                this.getProjectConversationParticipantDtos(),
                this.agentApiMapper.map(this.getProjectCreatedAt()),
                this.agentApiMapper.map(this.getProjectUpdatedAt())
        );
    }

    private ProjectConversationDTO getProjectConversationDto(
            final List<ProjectConversationParticipantDTO> participants,
            final OffsetDateTime createdAt,
            final OffsetDateTime updatedAt
    ) {
        return ProjectConversationDTO.builder()
                .id(this.getProjectConversationId())
                .projectId(this.getProjectId())
                .type(ProjectConversationDTO.TypeEnum.MULTI_AGENT)
                .title(this.getConversationTitle())
                .status(ProjectConversationDTO.StatusEnum.ACTIVE)
                .participants(participants)
                .canSendMessages(Boolean.FALSE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastMessageAt(null)
                .build();
    }

    private ProjectConversationDetailsDTO getProjectConversationDetailsDto() {
        return this.getProjectConversationDetailsDto(
                this.getProjectConversationParticipantDtos(),
                List.of(),
                this.getProjectConversationProjectDto(),
                this.agentApiMapper.map(this.getProjectCreatedAt()),
                this.agentApiMapper.map(this.getProjectUpdatedAt())
        );
    }

    private ProjectConversationDetailsDTO getProjectConversationDetailsDto(
            final List<ProjectConversationParticipantDTO> participants,
            final List<AgentConversationMessageDTO> messages,
            final ProjectConversationProjectDTO project,
            final OffsetDateTime createdAt,
            final OffsetDateTime updatedAt
    ) {
        return ProjectConversationDetailsDTO.builder()
                .id(this.getProjectConversationId())
                .projectId(this.getProjectId())
                .project(project)
                .type(ProjectConversationDetailsDTO.TypeEnum.MULTI_AGENT)
                .title(this.getConversationTitle())
                .status(ProjectConversationDetailsDTO.StatusEnum.ACTIVE)
                .participants(participants)
                .messages(messages)
                .canSendMessages(Boolean.FALSE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastMessageAt(null)
                .build();
    }

    private ProjectConversationProjectDTO getProjectConversationProjectDto() {
        return ProjectConversationProjectDTO.builder()
                .id(this.getProjectId())
                .name(this.getProjectName())
                .context(this.getProjectContext())
                .build();
    }

    private List<ProjectConversationParticipantDTO> getProjectConversationParticipantDtos() {
        return List.of(this.getProjectConversationParticipantDto());
    }

    private ProjectConversationParticipantDTO getProjectConversationParticipantDto() {
        return ProjectConversationParticipantDTO.builder()
                .type(ProjectConversationParticipantDTO.TypeEnum.AGENT)
                .agentId(this.getParticipantAgentId())
                .name(this.getParticipantName())
                .description(this.getParticipantDescription())
                .status(ProjectConversationParticipantDTO.StatusEnum.ACTIVE)
                .build();
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
        return this.getDomainAgent(AgentStatus.DRAFT);
    }

    private Agent getDomainAgent(final AgentStatus status) {
        return Agent.builder()
                .id(this.getAgentId())
                .userId(7L)
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(status)
                .createdAt(this.getAgentCreatedAt())
                .updatedAt(this.getAgentUpdatedAt())
                .build();
    }

    private AgentDTO getApiAgent() {
        return this.getApiAgent(AgentDTO.StatusEnum.DRAFT);
    }

    private AgentDTO getApiAgent(final AgentDTO.StatusEnum status) {
        return AgentDTO.builder()
                .id(this.getAgentId())
                .name("My agent")
                .description("My description")
                .instruction("My instruction")
                .status(status)
                .createdAt(OffsetDateTime.ofInstant(this.getAgentCreatedAt(), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(this.getAgentUpdatedAt(), ZoneOffset.UTC))
                .build();
    }

    private Agent getDomainAgentWithNullStatus() {
        return this.getDomainAgent(null);
    }

    private AgentDTO getApiAgentWithNullStatus() {
        return this.getApiAgent(null);
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
                .inputMessageId(UUID.fromString("56fca8c8-6f35-408c-9f6d-4929ab6f2467"))
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

    private ChatExecution getSubmitConversationExecutionWithoutRuntimeDispatch() {
        return ChatExecution.builder()
                .conversationId(UUID.fromString("5bddb194-5ca2-4461-9b6b-c5f986fa86ea"))
                .inputMessageId(UUID.fromString("56fca8c8-6f35-408c-9f6d-4929ab6f2467"))
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

    private UUID getAgentId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    private UUID getProjectConversationId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    private UUID getProjectId() {
        return UUID.fromString("22222222-2222-2222-2222-222222222222");
    }

    private UUID getParticipantAgentId() {
        return UUID.fromString("33333333-3333-3333-3333-333333333333");
    }

    private String getProjectName() {
        return "Sitionix";
    }

    private String getProjectContext() {
        return "Project context";
    }

    private String getConversationTitle() {
        return "Team chat";
    }

    private String getParticipantName() {
        return "Writer";
    }

    private String getParticipantDescription() {
        return "Writes copy";
    }

    private Instant getProjectCreatedAt() {
        return Instant.parse("2026-05-08T10:00:00Z");
    }

    private Instant getProjectUpdatedAt() {
        return Instant.parse("2026-05-08T10:01:00Z");
    }

    private Instant getAgentCreatedAt() {
        return Instant.parse("2026-01-10T10:15:30Z");
    }

    private Instant getAgentUpdatedAt() {
        return Instant.parse("2026-01-10T10:20:30Z");
    }

}
