package com.example.springApp.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedUserTest {

    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser();

    @Test
    void idAcceptsLongAndStringPrincipals() {
        Authentication longAuthentication = mock(Authentication.class);
        Authentication stringAuthentication = mock(Authentication.class);

        when(longAuthentication.getPrincipal()).thenReturn(42L);
        when(stringAuthentication.getPrincipal()).thenReturn("43");

        assertThat(authenticatedUser.id(longAuthentication)).isEqualTo(42L);
        assertThat(authenticatedUser.id(stringAuthentication)).isEqualTo(43L);
    }

    @Test
    void idRejectsUnsupportedPrincipalType() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new Object());

        assertThatThrownBy(() -> authenticatedUser.id(authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuario autenticado invalido");
    }
}
