package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentChatNotAllowedException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.domain.model.ChatAgentCommand;
import com.sitionix.atmssox.domain.model.ChatAgentResponse;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationStatus;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectConversationChatHandlerTest {

    private DirectConversationChatHandler directConversationChatHandler;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AgentRuleRepository agentRuleRepository;

    @Mock
    private ConversationMessageRepository conversationMessageRepository;

    @Mock
    private ConversationContextBuilder conversationContextBuilder;

    @Mock
    private AgentExecutionService agentExecutionService;

    @Mock
    private AgentExecutionHandlerRegistry agentExecutionHandlerRegistry;

    @Mock
    private AgentExecutionHandler agentExecutionHandler;

    @Mock
    private RuleSuggestionAnalysisTrigger ruleSuggestionAnalysisTrigger;

    @BeforeEach
    void setUp() {
        this.directConversationChatHandler = new DirectConversationChatHandler(
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.agentExecutionHandlerRegistry,
                this.agentExecutionService,
                this.ruleSuggestionAnalysisTrigger
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.agentExecutionHandlerRegistry,
                this.agentExecutionHandler,
                this.agentExecutionService
        );
    }

    @Test
    void givenActiveAgentAndValidMessage_whenHandle_thenReturnReply() {
        //given
        final UUID conversationId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84cb");
        final UUID agentId = UUID.fromString("f4cc43fd-f2a3-4d8d-a3d6-56f26fbe84ca");
        final Conversation conversation = this.getConversation(conversationId);
        final List<ConversationParticipant> participants = this.getParticipants(conversationId, agentId);
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message("  Explain clean architecture.  ")
                .build();
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, "  Keep answers concise.  ");
        final ConversationMessage userMessage = this.getMessage(
                conversationId,
                ConversationParticipantType.USER,
                "17",
                "Explain clean architecture."
        );
        final ConversationMessage replyMessage = this.getMessage(
                conversationId,
                ConversationParticipantType.AGENT,
                agentId.toString(),
                "It separates business rules from external frameworks."
        );

        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentExecutionHandlerRegistry.getHandler(AgentType.USER)).thenReturn(this.agentExecutionHandler);
        when(this.agentExecutionHandler.supportedContextType()).thenReturn(UserAgentExecutionContext.class);
        when(this.conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(userMessage)
                .thenReturn(replyMessage);
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(userMessage));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null))
                .thenReturn(List.of());
        when(this.conversationContextBuilder.build(List.of(), List.of(userMessage))).thenReturn("context-prompt");
        when(this.agentExecutionService.execute(any(Agent.class), any(AgentExecutionContext.class)))
                .thenReturn("It separates business rules from external frameworks.");
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        //when
        final ChatAgentResponse actual = this.directConversationChatHandler.handle(conversation, participants, command, 17L);

        //then
        assertThat(actual.getConversationId()).isEqualTo(conversationId);
        assertThat(actual.getReply()).isEqualTo(replyMessage);
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentExecutionHandlerRegistry).getHandler(AgentType.USER);
        verify(this.agentExecutionHandler).supportedContextType();
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null);
        verify(this.conversationMessageRepository, times(2)).save(any(ConversationMessage.class));
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        verify(this.conversationContextBuilder).build(List.of(), List.of(userMessage));
        verify(this.agentExecutionService).execute(any(Agent.class), any(AgentExecutionContext.class));
        verify(this.conversationRepository).save(any(Conversation.class));
    }

    @Test
    void givenNullInstruction_whenHandle_thenExecuteWithEmptyInstruction() {
        //given
        final UUID conversationId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebfa");
        final UUID agentId = UUID.fromString("4fc4d3e9-8f2b-444e-9ff5-a2ea960cebf9");
        final Conversation conversation = this.getConversation(conversationId);
        final List<ConversationParticipant> participants = this.getParticipants(conversationId, agentId);
        final ChatAgentCommand command = ChatAgentCommand.builder()
                .conversationId(conversationId)
                .message("hello")
                .build();
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, null);
        final ConversationMessage userMessage = this.getMessage(conversationId, ConversationParticipantType.USER, "17", "hello");
        final ConversationMessage replyMessage = this.getMessage(conversationId, ConversationParticipantType.AGENT, agentId.toString(), "hi");

        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentExecutionHandlerRegistry.getHandler(AgentType.USER)).thenReturn(this.agentExecutionHandler);
        when(this.agentExecutionHandler.supportedContextType()).thenReturn(UserAgentExecutionContext.class);
        when(this.conversationMessageRepository.save(any(ConversationMessage.class)))
                .thenReturn(userMessage)
                .thenReturn(replyMessage);
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId))
                .thenReturn(List.of(userMessage));
        when(this.agentRuleRepository.findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null))
                .thenReturn(List.of());
        when(this.conversationContextBuilder.build(List.of(), List.of(userMessage))).thenReturn("context-prompt");
        when(this.agentExecutionService.execute(any(Agent.class), any(AgentExecutionContext.class))).thenReturn("hi");
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        //when
        final ChatAgentResponse actual = this.directConversationChatHandler.handle(conversation, participants, command, 17L);

        //then
        assertThat(actual.getConversationId()).isEqualTo(conversationId);
        assertThat(actual.getReply()).isEqualTo(replyMessage);
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentExecutionHandlerRegistry).getHandler(AgentType.USER);
        verify(this.agentExecutionHandler).supportedContextType();
        verify(this.agentRuleRepository).findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(agentId, 17L, AgentRuleStatus.ACTIVE, null);
        verify(this.conversationMessageRepository, times(2)).save(any(ConversationMessage.class));
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        verify(this.conversationContextBuilder).build(List.of(), List.of(userMessage));
        verify(this.agentExecutionService).execute(any(Agent.class), any(AgentExecutionContext.class));
        verify(this.conversationRepository).save(any(Conversation.class));
    }

    @Test
    void givenConversationWithoutAgentParticipant_whenHandle_thenThrowNotFoundException() {
        //given
        final UUID conversationId = UUID.fromString("a28ea11a-aa2c-4ec8-b790-9b6c9b7f5639");
        final Conversation conversation = this.getConversation(conversationId);
        final List<ConversationParticipant> participants = List.of(
                ConversationParticipant.builder()
                        .id(UUID.fromString("0a6fe9f8-78cb-4f22-8998-a6f53991d6fa"))
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.USER)
                        .participantId("17")
                        .joinedAt(Instant.parse("2026-04-20T08:05:00Z"))
                        .build()
        );

        //when
        //then
        assertThatThrownBy(() -> this.directConversationChatHandler.handle(
                conversation,
                participants,
                ChatAgentCommand.builder().message("hello").build(),
                17L
        ))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found");
        verifyNoInteractions(
                this.agentRepository,
                this.agentRuleRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.agentExecutionHandlerRegistry,
                this.agentExecutionHandler,
                this.agentExecutionService
        );
    }

    @Test
    void givenAgentNotActive_whenHandle_thenThrowChatNotAllowed() {
        //given
        final UUID conversationId = UUID.fromString("a88c40e3-28e5-4a9a-a292-83cc10c4f794");
        final UUID agentId = UUID.fromString("2de9b2c2-b477-430f-8f6c-e3269fce0ef1");
        final Conversation conversation = this.getConversation(conversationId);
        final List<ConversationParticipant> participants = this.getParticipants(conversationId, agentId);
        final Agent agent = this.getAgent(AgentStatus.DRAFT, "Instruction");

        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));

        //when
        //then
        assertThatThrownBy(() -> this.directConversationChatHandler.handle(
                conversation,
                participants,
                ChatAgentCommand.builder().message("hello").build(),
                17L
        ))
                .isInstanceOf(AgentChatNotAllowedException.class)
                .hasMessage("Only ACTIVE agent can execute chat");
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verifyNoInteractions(
                this.agentRuleRepository,
                this.conversationRepository,
                this.conversationMessageRepository,
                this.conversationContextBuilder,
                this.agentExecutionHandlerRegistry,
                this.agentExecutionHandler,
                this.agentExecutionService
        );
    }

    @Test
    void givenBlankMessage_whenHandle_thenThrowValidationException() {
        //given
        final UUID conversationId = UUID.fromString("704f2908-c499-4669-8fa5-c8cc37f4620f");
        final UUID agentId = UUID.fromString("d2d5ce96-e702-491e-b77f-d75fd03667a5");
        final Conversation conversation = this.getConversation(conversationId);
        final List<ConversationParticipant> participants = this.getParticipants(conversationId, agentId);
        final Agent agent = this.getAgent(AgentStatus.ACTIVE, "Instruction");

        when(this.agentRepository.findVisibleByIdAndUserId(agentId, 17L)).thenReturn(Optional.of(agent));
        when(this.agentExecutionHandlerRegistry.getHandler(AgentType.USER)).thenReturn(this.agentExecutionHandler);
        when(this.agentExecutionHandler.supportedContextType()).thenReturn(UserAgentExecutionContext.class);

        //when
        //then
        assertThatThrownBy(() -> this.directConversationChatHandler.handle(
                conversation,
                participants,
                ChatAgentCommand.builder().message("   ").build(),
                17L
        ))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("Message must not be blank");
        verify(this.agentRepository).findVisibleByIdAndUserId(agentId, 17L);
        verify(this.agentExecutionHandlerRegistry).getHandler(AgentType.USER);
        verify(this.agentExecutionHandler).supportedContextType();
        verify(this.conversationMessageRepository, never()).save(any(ConversationMessage.class));
        verifyNoInteractions(
                this.agentRuleRepository,
                this.conversationRepository,
                this.conversationContextBuilder,
                this.agentExecutionService
        );
    }

    private Conversation getConversation(final UUID conversationId) {
        return Conversation.builder()
                .id(conversationId)
                .userId(17L)
                .title("Explain clean architecture.")
                .type(ConversationType.DIRECT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.parse("2026-04-20T08:05:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:05:00Z"))
                .lastMessageAt(Instant.parse("2026-04-20T08:05:00Z"))
                .build();
    }

    private List<ConversationParticipant> getParticipants(final UUID conversationId, final UUID agentId) {
        return List.of(
                ConversationParticipant.builder()
                        .id(UUID.fromString("d74413f8-a5c9-48ec-a0f1-f62f02f27729"))
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.USER)
                        .participantId("17")
                        .joinedAt(Instant.parse("2026-04-20T08:05:00Z"))
                        .build(),
                ConversationParticipant.builder()
                        .id(UUID.fromString("8cb8741f-d2df-41b2-95e7-76f09dff2dbf"))
                        .conversationId(conversationId)
                        .participantType(ConversationParticipantType.AGENT)
                        .participantId(agentId.toString())
                        .joinedAt(Instant.parse("2026-04-20T08:05:00Z"))
                        .build()
        );
    }

    private ConversationMessage getMessage(final UUID conversationId,
                                           final ConversationParticipantType authorType,
                                           final String authorId,
                                           final String content) {
        return ConversationMessage.builder()
                .id(UUID.fromString("8a74f23d-ab2e-4ac8-b656-c76deec45f4f"))
                .conversationId(conversationId)
                .authorType(authorType)
                .authorId(authorId)
                .content(content)
                .createdAt(Instant.parse("2026-04-20T08:06:00Z"))
                .build();
    }

    private Agent getAgent(final AgentStatus status, final String instruction) {
        return Agent.builder()
                .id(UUID.fromString("8a74f23d-ab2e-4ac8-b656-c76deec45f4e"))
                .userId(17L)
                .name("Architecture Agent")
                .description("Description")
                .instruction(instruction)
                .status(status)
                .createdAt(Instant.parse("2026-04-20T08:00:00Z"))
                .updatedAt(Instant.parse("2026-04-20T08:01:00Z"))
                .build();
    }
}
