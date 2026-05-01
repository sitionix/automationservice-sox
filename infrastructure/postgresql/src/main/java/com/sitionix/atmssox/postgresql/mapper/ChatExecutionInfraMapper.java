package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.ChatExecution;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
                ChatExecutionStatusInfraMapper.class,
                ChatExecutionFailureClassInfraMapper.class
        }
)
public interface ChatExecutionInfraMapper {

    @Mapping(target = "executionId", source = "executionId")
    @Mapping(target = "failureClass", source = "failure", qualifiedByName = "toFailureClass")
    @Mapping(target = "failureReason", source = "failure", qualifiedByName = "toFailureReason")
    @Mapping(target = "failureRetryable", source = "failure", qualifiedByName = "toFailureRetryable")
    ChatExecutionEntity asChatExecutionEntity(ChatExecution src);

    @Mapping(target = "idempotencyReplayed", constant = "false")
    @Mapping(target = "assistantMessage", ignore = true)
    @Mapping(target = "failure", source = ".", qualifiedByName = "toFailure")
    ChatExecution asChatExecution(ChatExecutionEntity src);

    @Named("toFailure")
    default ChatExecutionFailure toFailure(final ChatExecutionEntity src) {
        if (src.getFailureClass() == null) {
            return null;
        }
        return ChatExecutionFailure.builder()
                .failureClass(ChatExecutionFailureClass.fromId(src.getFailureClass().getId()))
                .reason(src.getFailureReason())
                .retryable(Boolean.TRUE.equals(src.getFailureRetryable()))
                .build();
    }

    @Named("toFailureClass")
    default ChatExecutionFailureClass toFailureClass(final ChatExecutionFailure src) {
        return src == null ? null : src.getFailureClass();
    }

    @Named("toFailureReason")
    default String toFailureReason(final ChatExecutionFailure src) {
        return src == null ? null : src.getReason();
    }

    @Named("toFailureRetryable")
    default Boolean toFailureRetryable(final ChatExecutionFailure src) {
        return src == null ? null : src.isRetryable();
    }
}
