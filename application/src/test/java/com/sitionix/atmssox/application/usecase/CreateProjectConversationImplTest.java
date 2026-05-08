package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ConversationType;
import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.ConversationParticipantRepository;
import com.sitionix.atmssox.domain.repository.ConversationRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProjectConversationImplTest {

    private CreateProjectConversationImpl createProjectConversation;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.createProjectConversation = new CreateProjectConversationImpl(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenSingleAttachedAgent_whenExecute_thenCreateDirectConversationWithParticipants() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID agentId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        final CreateProjectConversationCommand command = mock(CreateProjectConversationCommand.class);
        final AgentProject project = mock(AgentProject.class);
        final ProjectAgent projectAgent = mock(ProjectAgent.class);
        final Conversation savedConversation = mock(Conversation.class);

        when(command.getAgentIds()).thenReturn(List.of(agentId));
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, 17L)).thenReturn(List.of(projectAgent));
        when(projectAgent.getId()).thenReturn(agentId);
        when(projectAgent.getName()).thenReturn("Writer");
        when(projectAgent.getDescription()).thenReturn("Writes copy");
        when(projectAgent.getStatus()).thenReturn(AgentStatus.ACTIVE);
        when(this.conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
        when(savedConversation.getId()).thenReturn(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));

        //when
        final ProjectConversationDetails actual = this.createProjectConversation.execute(projectId, command);

        //then
        assertThat(actual.getConversation()).isEqualTo(savedConversation);
        assertThat(actual.getProject()).isEqualTo(project);
        assertThat(actual.getParticipants()).hasSize(1);
        assertThat(actual.getParticipants().get(0).getParticipantType()).isEqualTo(ConversationParticipantType.AGENT);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, 17L);
        verify(this.conversationRepository).save(any(Conversation.class));
        verify(this.conversationParticipantRepository).saveAll(any(List.class));
    }

    @Test
    void givenDuplicateAgentIds_whenExecute_thenThrowValidationException() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID agentId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        final CreateProjectConversationCommand command = mock(CreateProjectConversationCommand.class);
        when(command.getAgentIds()).thenReturn(List.of(agentId, agentId));

        //when
        //then
        assertThatThrownBy(() -> this.createProjectConversation.execute(projectId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("agentIds must contain unique items");
        verifyNoInteractions(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenNullCommand_whenExecute_thenThrowValidationException() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        //when
        //then
        assertThatThrownBy(() -> this.createProjectConversation.execute(projectId, null))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("agentIds must contain at least one item");
        verifyNoInteractions(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenEmptyAgentIds_whenExecute_thenThrowValidationException() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final CreateProjectConversationCommand command = mock(CreateProjectConversationCommand.class);
        when(command.getAgentIds()).thenReturn(List.of());

        //when
        //then
        assertThatThrownBy(() -> this.createProjectConversation.execute(projectId, command))
                .isInstanceOf(AgentValidationException.class)
                .hasMessage("agentIds must contain at least one item");
        verifyNoInteractions(
                this.agentProjectRepository,
                this.agentProjectMemberRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.authenticatedUserProvider
        );
    }
}
