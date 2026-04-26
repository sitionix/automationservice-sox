package com.sitionix.atmssox.application.usecase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForcePrRedStateTest {

    @Test
    void givenIntentionalFailure_whenRun_thenFailBuild() {
        //given
        final boolean given = false;

        //when
        final boolean actual = given;

        //then
        assertThat(actual).isTrue();
    }
}
