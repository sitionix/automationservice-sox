package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.ChatExecutionStatus;
import com.sitionix.atmssox.postgresql.entity.conversation.ChatExecutionStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatExecutionStatusInfraMapperTest {

    private ChatExecutionStatusInfraMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChatExecutionStatusInfraMapperImpl();
    }

    @Test
    void givenNullStatusEntity_whenAsStatus_thenReturnNull() {
        //given

        //when
        final ChatExecutionStatus result = this.mapper.asStatus(null);

        //then
        assertThat(result).isNull();
    }

    @Test
    void givenValidStatusEntity_whenAsStatus_thenReturnMappedStatus() {
        //given
        final ChatExecutionStatusEntity entity = ChatExecutionStatusEntity.builder()
                .id(2L)
                .description("IN_PROGRESS")
                .build();

        //when
        final ChatExecutionStatus result = this.mapper.asStatus(entity);

        //then
        assertThat(result).isEqualTo(ChatExecutionStatus.IN_PROGRESS);
    }

    @Test
    void givenNullStatus_whenAsStatusEntity_thenReturnNull() {
        //given

        //when
        final ChatExecutionStatusEntity result = this.mapper.asStatusEntity(null);

        //then
        assertThat(result).isNull();
    }

    @Test
    void givenValidStatus_whenAsStatusEntity_thenReturnMappedStatusEntity() {
        //given

        //when
        final ChatExecutionStatusEntity result = this.mapper.asStatusEntity(ChatExecutionStatus.COMPLETED);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getDescription()).isEqualTo("COMPLETED");
    }
}
