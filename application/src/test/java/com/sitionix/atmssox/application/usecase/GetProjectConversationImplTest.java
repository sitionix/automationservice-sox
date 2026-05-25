package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationMessage;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.ConversationMessageRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectConversationImplTest {

    private GetProjectConversationImpl getProjectConversation;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;
    @Mock
    private ConversationMessageRepository conversationMessageRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.getProjectConversation = new GetProjectConversationImpl(
                this.agentProjectRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.agentProjectMemberRepository,
                this.authenticatedUserProvider
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(
                this.agentProjectRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
                this.conversationMessageRepository,
                this.agentProjectMemberRepository,
                this.authenticatedUserProvider
        );
    }

    @Test
    void givenProjectConversationWithAgentParticipant_whenExecute_thenReturnConversationDetails() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID conversationId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        final UUID agentId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        final AgentProject project = mock(AgentProject.class);
        final Conversation conversation = mock(Conversation.class);
        final ConversationParticipant participant = mock(ConversationParticipant.class);
        final ProjectAgent projectAgent = mock(ProjectAgent.class);
        final ConversationMessage message = mock(ConversationMessage.class);

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.conversationRepository.findActiveByIdAndUserIdAndProjectId(conversationId, 17L, projectId)).thenReturn(Optional.of(conversation));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, 17L)).thenReturn(List.of(projectAgent));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId)).thenReturn(List.of(participant));
        when(this.conversationMessageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(List.of(message));
        when(conversation.getId()).thenReturn(conversationId);
        when(projectAgent.getId()).thenReturn(agentId);
        when(projectAgent.getName()).thenReturn("Writer");
        when(projectAgent.getDescription()).thenReturn("Writes copy");
        when(projectAgent.getStatus()).thenReturn(AgentStatus.ACTIVE);
        when(participant.getParticipantType()).thenReturn(ConversationParticipantType.AGENT);
        when(participant.getParticipantId()).thenReturn(agentId.toString());
        when(participant.toBuilder()).thenReturn(ConversationParticipant.builder());

        //when
        final ProjectConversationDetails actual = this.getProjectConversation.execute(projectId, conversationId);

        //then
        assertThat(actual.getConversation()).isEqualTo(conversation);
        assertThat(actual.getProject()).isEqualTo(project);
        assertThat(actual.getParticipants()).hasSize(1);
        assertThat(actual.getMessages()).containsExactly(message);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndProjectId(conversationId, 17L, projectId);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, 17L);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(this.conversationMessageRepository).findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        verify(conversation, times(2)).getId();
        verify(participant).getParticipantType();
        verify(participant).getParticipantId();
        verify(participant).toBuilder();
        verify(projectAgent).getId();
        verify(projectAgent).getName();
        verify(projectAgent).getDescription();
        verify(projectAgent).getStatus();
    }

    @Test
    void givenProjectNotFound_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID conversationId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getProjectConversation.execute(projectId, conversationId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenConversationNotFound_whenExecute_thenThrowNotFound() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID conversationId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        final AgentProject project = mock(AgentProject.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.conversationRepository.findActiveByIdAndUserIdAndProjectId(conversationId, 17L, projectId))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.getProjectConversation.execute(projectId, conversationId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Conversation not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.conversationRepository).findActiveByIdAndUserIdAndProjectId(conversationId, 17L, projectId);
    }
}
