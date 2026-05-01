package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionFailureClassEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionFailureClassInfraMapper {

    default ChatExecutionFailureClass asFailureClass(final ChatExecutionFailureClassEntity failureClassEntity) {
        if (isNull(failureClassEntity)) {
            return null;
        }
        return ChatExecutionFailureClass.fromId(failureClassEntity.getId());
    }

    default ChatExecutionFailureClassEntity asFailureClassEntity(final ChatExecutionFailureClass failureClass) {
        if (isNull(failureClass)) {
            return null;
        }
        return ChatExecutionFailureClassEntity.builder()
                .id(failureClass.getId())
                .description(failureClass.name())
                .build();
    }
}
