package com.sitionix.atmssox.api.mapper;

import com.app_afesox.atmssox.api_first.dto.ChatExecutionFailureDTO;
import com.sitionix.atmssox.domain.model.ChatExecutionFailure;
import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatExecutionFailureApiMapperTest {

    private ChatExecutionFailureApiMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChatExecutionFailureApiMapperImpl();
    }

    @Test
    void givenChatExecutionFailure_whenAsChatExecutionFailureDto_thenReturnMappedDto() {
        //given
        final ChatExecutionFailure given = ChatExecutionFailure.builder()
                .failureClass(ChatExecutionFailureClass.EXECUTION_ERROR)
                .reason("Execution failed")
                .retryable(true)
                .build();

        //when
        final ChatExecutionFailureDTO actual = this.mapper.asChatExecutionFailureDto(given);

        //then
        assertThat(actual).isEqualTo(ChatExecutionFailureDTO.builder()
                .code("EXECUTION_ERROR")
                .message("Execution failed")
                .details(Map.of("retryable", true))
                .build());
    }
}
