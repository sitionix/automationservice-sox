package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionStatusInfraMapper {

    default ChatExecutionStatus asStatus(final ChatExecutionStatusEntity statusEntity) {
        if (isNull(statusEntity)) {
            return null;
        }
        return ChatExecutionStatus.fromId(statusEntity.getId());
    }

    default ChatExecutionStatusEntity asStatusEntity(final ChatExecutionStatus status) {
        if (isNull(status)) {
            return null;
        }
        return ChatExecutionStatusEntity.builder()
                .id(status.getId())
                .description(status.name())
                .build();
    }
}
