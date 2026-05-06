package com.sitionix.atmssox.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentProjectStatusTest {

    @Test
    void givenNullId_whenFromId_thenReturnNull() {
        //when
        final AgentProjectStatus actual = AgentProjectStatus.fromId(null);

        //then
        assertThat(actual).isNull();
    }

    @Test
    void givenValidId_whenFromId_thenReturnStatus() {
        //when
        final AgentProjectStatus actual = AgentProjectStatus.fromId(1L);

        //then
        assertThat(actual).isEqualTo(AgentProjectStatus.ACTIVE);
    }

    @Test
    void givenUnknownId_whenFromId_thenThrowIllegalArgumentException() {
        //when/then
        assertThatThrownBy(() -> AgentProjectStatus.fromId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown AgentProjectStatus id: 99");
    }
}
