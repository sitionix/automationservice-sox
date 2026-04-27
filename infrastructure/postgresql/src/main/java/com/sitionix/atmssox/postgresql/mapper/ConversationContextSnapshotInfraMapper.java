package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.ConversationContextSnapshot;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationContextSnapshotEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ConversationContextSnapshotInfraMapper {

    @Mapping(target = "conversation.conversationId", source = "conversationId")
    ConversationContextSnapshotEntity asConversationContextSnapshotEntity(ConversationContextSnapshot snapshot);

    @Mapping(target = "conversationId", source = "conversation.conversationId")
    ConversationContextSnapshot asConversationContextSnapshot(ConversationContextSnapshotEntity snapshotEntity);
}
