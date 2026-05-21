package com.example.springApp.controller;

import com.example.springApp.dto.AuthStatusResponse;
import com.example.springApp.dto.LoginRequest;
import com.example.springApp.dto.RegisterRequest;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.User;
import com.example.springApp.security.JwtAuthenticationFilter;
import com.example.springApp.service.AuthService;
import com.example.springApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticacao", description = "Login OAuth2, usuario autenticado e encerramento de sessao.")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final ResponseMapper responseMapper;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public AuthController(
            UserService userService,
            AuthService authService,
            ResponseMapper responseMapper,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.userService = userService;
        this.authService = authService;
        this.responseMapper = responseMapper;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Criar conta com email e senha")
    public AuthStatusResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        User user = authService.register(request);
        String token = authService.login(new LoginRequest(request.email(), request.password()));
        setAuthCookie(httpRequest, httpResponse, token);
        return new AuthStatusResponse(true, responseMapper.toUserResponse(user));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Entrar com email e senha")
    public AuthStatusResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String token = authService.login(request);
        User user = userService.getUserByEmail(request.email());
        setAuthCookie(httpRequest, httpResponse, token);
        return new AuthStatusResponse(true, responseMapper.toUserResponse(user));
    }

    @GetMapping("/auth/status")
    @Operation(
            summary = "Consultar status da sessao",
            description = "Retorna authenticated=false quando nao houver JWT valido e dados do usuario quando houver."
    )
    public AuthStatusResponse status(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return new AuthStatusResponse(false, null);
        }

        return new AuthStatusResponse(
                true,
                responseMapper.toUserResponse(userService.getUserById(userId))
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar sessao", description = "Remove o cookie HTTP-only usado pelo login OAuth2.")
    @ApiResponse(responseCode = "204", description = "Sessao encerrada")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        // Expira o cookie no mesmo caminho usado no login OAuth2.
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    private void setAuthCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
    }
}
