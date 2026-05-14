package com.example.springApp.controller;

import com.example.springApp.dto.AuthStatusResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.security.JwtAuthenticationFilter;
import com.example.springApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticacao", description = "Login OAuth2, usuario autenticado e encerramento de sessao.")
public class AuthController {

    private final UserService userService;
    private final ResponseMapper responseMapper;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public AuthController(
            UserService userService,
            ResponseMapper responseMapper,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.userService = userService;
        this.responseMapper = responseMapper;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
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
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
