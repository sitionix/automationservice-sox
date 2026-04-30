package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ChatExecutionFailureDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionFailureApiMapper {

    @Mapping(target = "code", expression = "java(failure.getFailureClass() == null ? null : failure.getFailureClass().name())")
    @Mapping(target = "message", source = "reason")
    @Mapping(target = "details", expression = "java(java.util.Map.of(\"retryable\", failure.isRetryable()))")
    ChatExecutionFailureDTO asChatExecutionFailureDto(ChatExecutionFailure failure);
}
