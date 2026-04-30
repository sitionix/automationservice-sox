package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ChatExecutionFailureDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionFailureApiMapper {

    default ChatExecutionFailureDTO asChatExecutionFailureDto(final ChatExecutionFailure failure) {
        if (failure == null) {
            return null;
        }
        return ChatExecutionFailureDTO.builder()
                .code(failure.getFailureClass() == null ? null : failure.getFailureClass().name())
                .message(failure.getReason())
                .details(Map.of("retryable", failure.isRetryable()))
                .build();
    }
}
