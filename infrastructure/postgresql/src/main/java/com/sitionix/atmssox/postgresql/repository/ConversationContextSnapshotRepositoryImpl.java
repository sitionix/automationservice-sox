package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.domain.repository.ConversationContextSnapshotRepository;
import com.sitionix.atmssox.postgresql.jpa.ConversationContextSnapshotJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.ConversationContextSnapshotInfraMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConversationContextSnapshotRepositoryImpl implements ConversationContextSnapshotRepository {

    private final ConversationContextSnapshotJpaRepository conversationContextSnapshotJpaRepository;
    private final ConversationContextSnapshotInfraMapper conversationContextSnapshotInfraMapper;

    @Override
    public ConversationContextSnapshot save(final ConversationContextSnapshot snapshot) {
        return this.conversationContextSnapshotInfraMapper.asConversationContextSnapshot(
                this.conversationContextSnapshotJpaRepository.save(
                        this.conversationContextSnapshotInfraMapper.asConversationContextSnapshotEntity(snapshot)
                )
        );
    }

    @Override
    public Optional<ConversationContextSnapshot> findByConversationId(final UUID conversationId) {
        return this.conversationContextSnapshotJpaRepository.findByConversationConversationId(conversationId)
                .map(this.conversationContextSnapshotInfraMapper::asConversationContextSnapshot);
    }
}
