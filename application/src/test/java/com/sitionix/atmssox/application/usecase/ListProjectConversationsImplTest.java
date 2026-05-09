package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.Conversation;
import com.sitionix.atmssox.domain.model.ConversationParticipant;
import com.sitionix.atmssox.domain.model.ConversationParticipantType;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProjectConversationsImplTest {

    private ListProjectConversationsImpl listProjectConversations;

    @Mock
    private AgentProjectRepository agentProjectRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationParticipantRepository conversationParticipantRepository;
    @Mock
    private AgentProjectMemberRepository agentProjectMemberRepository;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @BeforeEach
    void setUp() {
        this.listProjectConversations = new ListProjectConversationsImpl(
                this.agentProjectRepository,
                this.conversationRepository,
                this.conversationParticipantRepository,
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

        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, 17L)).thenReturn(List.of(projectAgent));
        when(this.conversationRepository.findAllActiveByUserIdAndProjectId(17L, projectId)).thenReturn(List.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId)).thenReturn(List.of(participant));
        when(conversation.getId()).thenReturn(conversationId);
        when(projectAgent.getId()).thenReturn(agentId);
        when(projectAgent.getName()).thenReturn("Writer");
        when(projectAgent.getDescription()).thenReturn("Writes copy");
        when(projectAgent.getStatus()).thenReturn(AgentStatus.ACTIVE);
        when(participant.getParticipantType()).thenReturn(ConversationParticipantType.AGENT);
        when(participant.getParticipantId()).thenReturn(agentId.toString());
        when(participant.toBuilder()).thenReturn(ConversationParticipant.builder());

        //when
        final List<ProjectConversationDetails> actual = this.listProjectConversations.execute(projectId);

        //then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getConversation()).isEqualTo(conversation);
        assertThat(actual.get(0).getProject()).isEqualTo(project);
        assertThat(actual.get(0).getParticipants()).hasSize(1);
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, 17L);
        verify(this.conversationRepository).findAllActiveByUserIdAndProjectId(17L, projectId);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(conversation).getId();
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
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> this.listProjectConversations.execute(projectId))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent project not found");
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
    }

    @Test
    void givenProjectConversationWithUserParticipant_whenExecute_thenExcludeParticipant() {
        //given
        final UUID projectId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        final UUID conversationId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        final AgentProject project = mock(AgentProject.class);
        final Conversation conversation = mock(Conversation.class);
        final ConversationParticipant participant = mock(ConversationParticipant.class);
        when(this.authenticatedUserProvider.getUserId()).thenReturn(17L);
        when(this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, 17L)).thenReturn(Optional.of(project));
        when(this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, 17L)).thenReturn(List.of());
        when(this.conversationRepository.findAllActiveByUserIdAndProjectId(17L, projectId)).thenReturn(List.of(conversation));
        when(this.conversationParticipantRepository.findAllByConversationId(conversationId)).thenReturn(List.of(participant));
        when(conversation.getId()).thenReturn(conversationId);
        when(participant.getParticipantType()).thenReturn(ConversationParticipantType.USER);

        //when
        final List<ProjectConversationDetails> actual = this.listProjectConversations.execute(projectId);

        //then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getParticipants()).isEmpty();
        verify(this.authenticatedUserProvider).getUserId();
        verify(this.agentProjectRepository).findVisibleByIdAndOwnerUserId(projectId, 17L);
        verify(this.agentProjectMemberRepository).findVisibleProjectAgents(projectId, 17L);
        verify(this.conversationRepository).findAllActiveByUserIdAndProjectId(17L, projectId);
        verify(this.conversationParticipantRepository).findAllByConversationId(conversationId);
        verify(conversation).getId();
        verify(participant).getParticipantType();
    }
}
