package com.example.springApp.security;

import com.example.springApp.config.FrontendOriginsProperties;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final FrontendOriginsProperties frontendOrigins;
    private final boolean authCookieSecure;
    private final String authCookieSameSite;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            FrontendOriginsProperties frontendOrigins,
            @Value("${app.auth.cookie-secure:false}") boolean authCookieSecure,
            @Value("${app.auth.cookie-same-site:Lax}") String authCookieSameSite
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendOrigins = frontendOrigins;
        this.authCookieSecure = authCookieSecure;
        this.authCookieSameSite = authCookieSameSite;
    }

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
        Boolean emailVerified = oauthUser.getAttribute("email_verified");

        if (oauthId == null || email == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new ServletException("Conta Google sem email verificado");
        }

        // Vincula por oauthId quando possivel; email cobre usuarios criados antes do primeiro login Google.
        User user = userRepository.findByOauthId(oauthId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> updateGoogleData(existingUser, oauthId, email, name))
                .orElseGet(() -> createUser(oauthId, email, name));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
        ResponseCookie authCookie = ResponseCookie.from(JwtAuthenticationFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(authCookieSecure || request.isSecure())
                .sameSite(authCookieSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();
        // O frontend centraliza o tratamento pos-login nesta rota.
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendOrigins.primaryOrigin())
                .path("/oauth2/callback")
                .build()
                .toUriString();

        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
        response.sendRedirect(redirectUrl);
    }

    private User createUser(String oauthId, String email, String name) {
        User user = User.builder()
                .oauthId(oauthId)
                .email(email)
                .nome(name)
                .build();
        return userRepository.save(user);
    }

    private User updateGoogleData(User user, String oauthId, String email, String name) {
        user.setOauthId(oauthId);
        user.setEmail(email);
        user.setNome(name);
        return userRepository.save(user);
    }
}
