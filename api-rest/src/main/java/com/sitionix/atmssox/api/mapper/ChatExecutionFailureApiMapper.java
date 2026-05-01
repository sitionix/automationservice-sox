package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ChatExecutionFailureDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import java.util.Map;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionFailureApiMapper {

    @Mapping(target = "code", expression = "java(failure.getFailureClass() == null ? null : failure.getFailureClass().name())")
    @Mapping(target = "message", source = "reason")
    @Mapping(target = "details", source = "retryable")
    ChatExecutionFailureDTO asChatExecutionFailureDto(ChatExecutionFailure failure);

    default Map<String, Object> mapDetails(final boolean retryable) {
        return Map.of("retryable", retryable);
    }
}
