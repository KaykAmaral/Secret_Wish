package com.example.springApp.controller;

import com.example.springApp.dto.AuthTokenResponse;
import com.example.springApp.mapper.ResponseMapper;
import com.example.springApp.model.User;
import com.example.springApp.repository.UserRepository;
import com.example.springApp.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@ConditionalOnProperty(prefix = "app.dev-auth", name = "enabled", havingValue = "true")
@Transactional
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ResponseMapper responseMapper;

    public DevAuthController(
            UserRepository userRepository,
            JwtService jwtService,
            ResponseMapper responseMapper
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public Map<String, String> help() {
        return Map.of(
                "message", "Use /api/dev/token para gerar um JWT local de desenvolvimento.",
                "example", "/api/dev/token?nome=Kayky&email=kayky@example.com"
        );
    }

    @GetMapping("/token")
    public AuthTokenResponse tokenFromBrowser(
            @RequestParam(defaultValue = "Dev User") String nome,
            @RequestParam(defaultValue = "dev@example.com") String email
    ) {
        return issueToken(nome, email);
    }

    @PostMapping("/token")
    public AuthTokenResponse token(
            @RequestParam(defaultValue = "Dev User") String nome,
            @RequestParam(defaultValue = "dev@example.com") String email
    ) {
        return issueToken(nome, email);
    }

    private AuthTokenResponse issueToken(String nome, String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedName = nome == null || nome.isBlank() ? "Dev User" : nome.trim();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.save(User.builder()
                        .nome(normalizedName)
                        .email(normalizedEmail)
                        .oauthId("dev-" + normalizedEmail)
                        .build()));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getNome());
        return new AuthTokenResponse(token, responseMapper.toUserResponse(user));
    }
}
