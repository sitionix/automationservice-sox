package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ExecutionStatusDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatExecutionStatusApiMapperTest {

    private ChatExecutionStatusApiMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChatExecutionStatusApiMapperImpl();
    }

    @Test
    void givenAllInternalExecutionStatuses_whenMap_thenReturnCanonicalExternalStatuses() {
        //given
        final ChatExecutionStatus queued = ChatExecutionStatus.QUEUED;
        final ChatExecutionStatus inProgress = ChatExecutionStatus.IN_PROGRESS;
        final ChatExecutionStatus completed = ChatExecutionStatus.COMPLETED;
        final ChatExecutionStatus failed = ChatExecutionStatus.FAILED;

        //when
        final ExecutionStatusDTO queuedActual = this.mapper.map(queued);
        final ExecutionStatusDTO inProgressActual = this.mapper.map(inProgress);
        final ExecutionStatusDTO completedActual = this.mapper.map(completed);
        final ExecutionStatusDTO failedActual = this.mapper.map(failed);

        //then
        assertThat(queuedActual).isEqualTo(ExecutionStatusDTO.ACCEPTED);
        assertThat(inProgressActual).isEqualTo(ExecutionStatusDTO.IN_PROGRESS);
        assertThat(completedActual).isEqualTo(ExecutionStatusDTO.SUCCEEDED);
        assertThat(failedActual).isEqualTo(ExecutionStatusDTO.FAILED);
    }

    @Test
    void givenNullStatus_whenMap_thenReturnNull() {
        //given
        final ChatExecutionStatus given = null;

        //when
        final ExecutionStatusDTO actual = this.mapper.map(given);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenDeprecatedDispatchSkippedStatusName_whenResolvingDomainEnum_thenThrowException() {
        //given
        final String given = "DISPATCH_SKIPPED";

        //when
        //then
        assertThatThrownBy(() -> ChatExecutionStatus.valueOf(given))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
