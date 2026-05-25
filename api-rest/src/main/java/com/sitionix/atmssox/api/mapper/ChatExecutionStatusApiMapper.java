package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ExecutionStatusDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatExecutionStatusApiMapper {

    default ExecutionStatusDTO map(final ChatExecutionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case QUEUED -> ExecutionStatusDTO.ACCEPTED;
            case IN_PROGRESS -> ExecutionStatusDTO.IN_PROGRESS;
            case COMPLETED -> ExecutionStatusDTO.SUCCEEDED;
            case FAILED -> ExecutionStatusDTO.FAILED;
            case DISPATCH_SKIPPED -> ExecutionStatusDTO.DISPATCH_SKIPPED;
        };
    }
}
