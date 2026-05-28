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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticacao", description = "Login OAuth2, usuario autenticado e encerramento de sessao.")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthService authService;
    private final com.example.springApp.security.JwtService jwtService;
    private final ResponseMapper responseMapper;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public AuthController(
            UserService userService,
            AuthService authService,
            com.example.springApp.security.JwtService jwtService,
            ResponseMapper responseMapper,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.userService = userService;
        this.authService = authService;
        this.jwtService = jwtService;
        this.responseMapper = responseMapper;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Criar conta com email e senha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta criada e sessao iniciada"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ErroPadrao"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/ErroPadrao")
    })
    public AuthStatusResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Tentativa de registro para email: {}", request.email());
        User user = authService.register(request);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
        setAuthCookie(httpRequest, httpResponse, token);
        log.info("Usuario registrado e logado com sucesso: {}", request.email());
        return new AuthStatusResponse(true, responseMapper.toUserResponse(user));
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Entrar com email e senha")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado e cookie de sessao definido"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ErroPadrao"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/NaoAutorizado")
    })
    public AuthStatusResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("Tentativa de login para email: {}", request.email());
        String token = authService.login(request);
        User user = userService.getUserByEmail(request.email());
        setAuthCookie(httpRequest, httpResponse, token);
        log.info("Login realizado com sucesso para email: {}", request.email());
        return new AuthStatusResponse(true, responseMapper.toUserResponse(user));
    }

    @GetMapping("/auth/status")
    @Operation(
            summary = "Consultar status da sessao",
            description = "Retorna authenticated=false quando nao houver JWT valido e dados do usuario quando houver."
    )
    @ApiResponse(responseCode = "200", description = "Status da sessao retornado")
    public ResponseEntity<AuthStatusResponse> status(Authentication authentication) {
        AuthStatusResponse body;
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            body = new AuthStatusResponse(false, null);
        } else {
            body = new AuthStatusResponse(
                    true,
                    responseMapper.toUserResponse(userService.getUserById(userId))
            );
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(body);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar sessao", description = "Remove o cookie HTTP-only usado pelo login OAuth2.")
    @ApiResponse(responseCode = "204", description = "Sessao encerrada")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        ResponseCookie expiredCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    /**
     * Grava o JWT em cookie HTTP-only para reduzir exposicao do token ao JavaScript do navegador.
     */
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
