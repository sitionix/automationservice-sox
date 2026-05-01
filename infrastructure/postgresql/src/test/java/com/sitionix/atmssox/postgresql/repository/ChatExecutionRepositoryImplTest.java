package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import com.sitionix.atmssox.postgresql.jpa.ChatExecutionJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.ChatExecutionInfraMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatExecutionRepositoryImplTest {

    private ChatExecutionRepositoryImpl chatExecutionRepository;

    @Mock
    private ChatExecutionJpaRepository chatExecutionJpaRepository;

    @Mock
    private ChatExecutionInfraMapper chatExecutionInfraMapper;

    @BeforeEach
    void setUp() {
        this.chatExecutionRepository = new ChatExecutionRepositoryImpl(this.chatExecutionJpaRepository, this.chatExecutionInfraMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.chatExecutionJpaRepository, this.chatExecutionInfraMapper);
    }

    @Test
    void givenExecution_whenSave_thenReturnMappedSavedExecution() {
        //given
        final ChatExecution execution = org.mockito.Mockito.mock(ChatExecution.class);
        final ChatExecutionEntity mappedEntity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecutionEntity persistedEntity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionInfraMapper.asChatExecutionEntity(execution)).thenReturn(mappedEntity);
        when(this.chatExecutionJpaRepository.save(mappedEntity)).thenReturn(persistedEntity);
        when(this.chatExecutionInfraMapper.asChatExecution(persistedEntity)).thenReturn(expected);

        //when
        final ChatExecution actual = this.chatExecutionRepository.save(execution);

        //then
        assertThat(actual).isEqualTo(expected);
        verify(this.chatExecutionInfraMapper).asChatExecutionEntity(execution);
        verify(this.chatExecutionJpaRepository).save(mappedEntity);
        verify(this.chatExecutionInfraMapper).asChatExecution(persistedEntity);
    }

    @Test
    void givenExecutionId_whenFindByExecutionIdAndPresent_thenReturnMappedExecution() {
        //given
        final UUID executionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final ChatExecutionEntity entity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionJpaRepository.findById(executionId)).thenReturn(Optional.of(entity));
        when(this.chatExecutionInfraMapper.asChatExecution(entity)).thenReturn(expected);

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository.findByExecutionId(executionId);

        //then
        assertThat(actual).isEqualTo(Optional.of(expected));
        verify(this.chatExecutionJpaRepository).findById(executionId);
        verify(this.chatExecutionInfraMapper).asChatExecution(entity);
    }

    @Test
    void givenExecutionId_whenFindByExecutionIdAndMissing_thenReturnEmpty() {
        //given
        final UUID executionId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(this.chatExecutionJpaRepository.findById(executionId)).thenReturn(Optional.empty());

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository.findByExecutionId(executionId);

        //then
        assertThat(actual).isEqualTo(Optional.empty());
        verify(this.chatExecutionJpaRepository).findById(executionId);
    }

    @Test
    void givenAgentIdAndExecutionId_whenFindByAgentIdAndExecutionId_thenReturnMappedExecution() {
        //given
        final UUID agentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final UUID executionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final ChatExecutionEntity entity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionJpaRepository.findByAgentIdAndExecutionId(agentId, executionId)).thenReturn(Optional.of(entity));
        when(this.chatExecutionInfraMapper.asChatExecution(entity)).thenReturn(expected);

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository.findByAgentIdAndExecutionId(agentId, executionId);

        //then
        assertThat(actual).isEqualTo(Optional.of(expected));
        verify(this.chatExecutionJpaRepository).findByAgentIdAndExecutionId(agentId, executionId);
        verify(this.chatExecutionInfraMapper).asChatExecution(entity);
    }

    @Test
    void givenConversationIdAndIdempotencyKey_whenFindByConversationIdAndIdempotencyKey_thenReturnMappedExecution() {
        //given
        final UUID conversationId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        final String idempotencyKey = "idem-1";
        final ChatExecutionEntity entity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionJpaRepository.findByConversationIdAndIdempotencyKey(conversationId, idempotencyKey))
                .thenReturn(Optional.of(entity));
        when(this.chatExecutionInfraMapper.asChatExecution(entity)).thenReturn(expected);

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository
                .findByConversationIdAndIdempotencyKey(conversationId, idempotencyKey);

        //then
        assertThat(actual).isEqualTo(Optional.of(expected));
        verify(this.chatExecutionJpaRepository).findByConversationIdAndIdempotencyKey(conversationId, idempotencyKey);
        verify(this.chatExecutionInfraMapper).asChatExecution(entity);
    }

    @Test
    void givenUserIdAgentIdAndIdempotencyKey_whenFindByUserIdAndAgentIdAndIdempotencyKey_thenReturnMappedExecution() {
        //given
        final Long userId = 9L;
        final UUID agentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final String idempotencyKey = "idem-2";
        final ChatExecutionEntity entity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionJpaRepository.findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, idempotencyKey))
                .thenReturn(Optional.of(entity));
        when(this.chatExecutionInfraMapper.asChatExecution(entity)).thenReturn(expected);

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository
                .findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, idempotencyKey);

        //then
        assertThat(actual).isEqualTo(Optional.of(expected));
        verify(this.chatExecutionJpaRepository).findByUserIdAndAgentIdAndIdempotencyKey(userId, agentId, idempotencyKey);
        verify(this.chatExecutionInfraMapper).asChatExecution(entity);
    }

    @Test
    void givenExecutionIdAndStatus_whenFindByExecutionIdAndStatus_thenReturnMappedExecution() {
        //given
        final UUID executionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final ChatExecutionStatus status = ChatExecutionStatus.IN_PROGRESS;
        final ChatExecutionEntity entity = org.mockito.Mockito.mock(ChatExecutionEntity.class);
        final ChatExecution expected = org.mockito.Mockito.mock(ChatExecution.class);

        when(this.chatExecutionJpaRepository.findByExecutionIdAndStatusId(executionId, status.getId())).thenReturn(Optional.of(entity));
        when(this.chatExecutionInfraMapper.asChatExecution(entity)).thenReturn(expected);

        //when
        final Optional<ChatExecution> actual = this.chatExecutionRepository.findByExecutionIdAndStatus(executionId, status);

        //then
        assertThat(actual).isEqualTo(Optional.of(expected));
        verify(this.chatExecutionJpaRepository).findByExecutionIdAndStatusId(executionId, status.getId());
        verify(this.chatExecutionInfraMapper).asChatExecution(entity);
    }
}
