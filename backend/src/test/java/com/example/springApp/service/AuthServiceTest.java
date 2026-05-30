package com.example.springApp.service;

import com.example.springApp.dto.LoginRequest;
import com.example.springApp.dto.RegisterRequest;
import com.example.springApp.exception.BusinessException;
import com.example.springApp.exception.ConflictException;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerHashesPasswordAndPersistsUser() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "secret123");

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        User saved = authService.register(request);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getNome()).isEqualTo("Ana");
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getPassword()).isEqualTo("hashed-password");
        verify(passwordEncoder).encode("secret123");
        verify(emailService).sendWelcomeEmail("Ana", "ana@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "secret123");

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email ja cadastrado");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any(), any());
    }

    @Test
    void loginReturnsJwtWhenCredentialsAreValid() {
        User user = User.builder()
                .id(7L)
                .nome("Ana")
                .email("ana@example.com")
                .password("hashed-password")
                .build();

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(7L, "ana@example.com", "Ana")).thenReturn("jwt-token");

        String token = authService.login(new LoginRequest("ana@example.com", "secret123"));

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void loginRejectsMissingUserInvalidPasswordAndOauthOnlyAccount() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "secret123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email ou senha invalidos");

        User oauthOnly = User.builder().email("oauth@example.com").password(null).build();
        when(userRepository.findByEmail("oauth@example.com")).thenReturn(Optional.of(oauthOnly));

        assertThatThrownBy(() -> authService.login(new LoginRequest("oauth@example.com", "secret123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("login pelo Google");

        User localUser = User.builder().email("ana@example.com").password("hashed-password").build();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(localUser));
        when(passwordEncoder.matches("wrong", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email ou senha invalidos");

        verify(jwtService, never()).generateToken(any(), any(), any());
    }
}
