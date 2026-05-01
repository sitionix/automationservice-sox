package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.ChatExecutionFailureClass;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionFailureClassEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatExecutionFailureClassInfraMapperTest {

    private ChatExecutionFailureClassInfraMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChatExecutionFailureClassInfraMapperImpl();
    }

    @Test
    void givenNullFailureClassEntity_whenAsFailureClass_thenReturnNull() {
        //given

        //when
        final ChatExecutionFailureClass result = this.mapper.asFailureClass(null);

        //then
        assertThat(result).isNull();
    }

    @Test
    void givenValidFailureClassEntity_whenAsFailureClass_thenReturnMappedFailureClass() {
        //given
        final ChatExecutionFailureClassEntity entity = ChatExecutionFailureClassEntity.builder()
                .id(4L)
                .description("IDEMPOTENCY_CONFLICT")
                .build();

        //when
        final ChatExecutionFailureClass result = this.mapper.asFailureClass(entity);

        //then
        assertThat(result).isEqualTo(ChatExecutionFailureClass.IDEMPOTENCY_CONFLICT);
    }

    @Test
    void givenNullFailureClass_whenAsFailureClassEntity_thenReturnNull() {
        //given

        //when
        final ChatExecutionFailureClassEntity result = this.mapper.asFailureClassEntity(null);

        //then
        assertThat(result).isNull();
    }

    @Test
    void givenValidFailureClass_whenAsFailureClassEntity_thenReturnMappedFailureClassEntity() {
        //given

        //when
        final ChatExecutionFailureClassEntity result = this.mapper.asFailureClassEntity(ChatExecutionFailureClass.EXECUTION_ERROR);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getDescription()).isEqualTo("EXECUTION_ERROR");
    }
}
