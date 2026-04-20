package com.sitionix.atmssox.application.security;

import com.sitionix.atmssox.domain.exception.AuthenticationRequiredException;
import com.sitionix.forge.security.server.user.ForgeUserClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserProviderTest {

    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private ForgeUserClient forgeUserClient;

    @BeforeEach
    void setUp() {
        this.authenticatedUserProvider = new AuthenticatedUserProvider(this.forgeUserClient);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(this.forgeUserClient);
    }

    @Test
    void givenForgeUserId_whenGetUserId_thenReturnUserId() {
        //given
        when(this.forgeUserClient.getUserId()).thenReturn(17L);

        //when
        final Long actual = this.authenticatedUserProvider.getUserId();

        //then
        assertThat(actual).isEqualTo(17L);
        verify(this.forgeUserClient).getUserId();
    }

    @Test
    void givenForgeUserClientThrows_whenGetUserId_thenThrowAuthenticationRequiredException() {
        //given
        when(this.forgeUserClient.getUserId()).thenThrow(new RuntimeException("No auth context"));

        //when
        //then
        assertThatThrownBy(() -> this.authenticatedUserProvider.getUserId())
                .isInstanceOf(AuthenticationRequiredException.class)
                .hasMessage("Authentication required");
        verify(this.forgeUserClient).getUserId();
    }
}
