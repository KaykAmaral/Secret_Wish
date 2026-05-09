package com.example.springApp.security;

import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendOrigin;

    public GoogleOAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${app.frontend.origin}") String frontendOrigin
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendOrigin = frontendOrigin;
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

        User user = userRepository.findByOauthId(oauthId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> updateGoogleData(existingUser, oauthId, email, name))
                .orElseGet(() -> createUser(oauthId, email, name));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendOrigin)
                .path("/oauth2/callback")
                .queryParam("token", token)
                .build()
                .toUriString();

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
