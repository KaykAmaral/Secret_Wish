package com.example.springApp.security;

import com.example.springApp.config.FrontendOriginsProperties;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.service.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FrontendOriginsProperties frontendOrigins;
    private final EmailService emailService;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            FrontendOriginsProperties frontendOrigins,
            EmailService emailService,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendOrigins = frontendOrigins;
        this.emailService = emailService;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
    }

    /**
     * Finaliza o login Google criando/atualizando o usuario local, emitindo cookie JWT e redirecionando ao frontend.
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String oauthId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        Boolean emailVerified = oauthUser.getAttribute("email_verified");

        if (oauthId == null || email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new ServletException("Conta Google sem email verificado");
        }

        Optional<User> existingUser = userRepository.findByOauthId(oauthId)
                .or(() -> userRepository.findByEmail(email));
        // Conta existente apenas atualiza dados do Google; boas-vindas ficam restritas a criacao real.
        User user = existingUser
                .map(currentUser -> updateGoogleData(currentUser, oauthId, email, name, picture))
                .orElseGet(() -> createUser(oauthId, email, name, picture));
        if (existingUser.isEmpty()) {
            sendWelcomeEmailAfterCommit(user);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendOrigins.primaryOrigin())
                .path("/oauth2/callback")
                .build()
                .toUriString();

        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
        response.sendRedirect(redirectUrl);
    }

    /**
     * Cria a conta local usando os dados verificados retornados pelo Google.
     */
    private User createUser(String oauthId, String email, String name, String picture) {
        User user = User.builder()
                .oauthId(oauthId)
                .email(email)
                .nome(name)
                .imagemUrl(picture)
                .build();
        return userRepository.save(user);
    }

    /**
     * Vincula ou atualiza dados Google sem sobrescrever foto definida manualmente pelo usuario.
     */
    private User updateGoogleData(User user, String oauthId, String email, String name, String picture) {
        user.setOauthId(oauthId);
        user.setEmail(email);
        user.setNome(name);
        if (user.getImagemUrl() == null) {
            user.setImagemUrl(picture);
        }
        return userRepository.save(user);
    }

    /**
     * Evita enviar boas-vindas para uma conta Google que ainda possa falhar no commit.
     */
    private void sendWelcomeEmailAfterCommit(User user) {
        Runnable sendEmail = () -> emailService.sendWelcomeEmail(user.getNome(), user.getEmail());
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // Mantem o metodo seguro caso seja reutilizado fora do fluxo transacional do OAuth2.
            sendEmail.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Evita enviar email se a criacao da conta OAuth2 for revertida.
                sendEmail.run();
            }
        });
    }
}
