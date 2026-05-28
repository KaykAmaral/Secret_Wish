package com.example.springApp.security;

import com.example.springApp.config.FrontendOriginsProperties;
import com.example.springApp.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Component
public class CookieCsrfProtectionFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final FrontendOriginsProperties frontendOrigins;
    private final ObjectMapper objectMapper;

    public CookieCsrfProtectionFilter(FrontendOriginsProperties frontendOrigins, ObjectMapper objectMapper) {
        this.frontendOrigins = frontendOrigins;
        this.objectMapper = objectMapper;
    }

    /**
     * Bloqueia mutacoes autenticadas por cookie quando a origem nao e uma origem de frontend confiavel.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (requiresOriginCheck(request) && !hasAllowedOrigin(request)) {
            ApiErrorResponse body = new ApiErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.FORBIDDEN.value(),
                    "Origem nao permitida",
                    "Requisicao com cookie de autenticacao exige origem confiavel",
                    Collections.emptyMap()
            );

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresOriginCheck(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod())
                && hasAuthCookie(request)
                && !hasBearerToken(request);
    }

    private boolean hasAuthCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (JwtAuthenticationFilter.AUTH_COOKIE_NAME.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Bearer ");
    }

    private boolean hasAllowedOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null) {
            return frontendOrigins.allowedOrigins().contains(origin);
        }

        String referer = request.getHeader(HttpHeaders.REFERER);
        return referer != null && frontendOrigins.allowedOrigins().contains(originFromReferer(referer));
    }

    private String originFromReferer(String referer) {
        try {
            URI uri = URI.create(referer);
            String port = uri.getPort() >= 0 ? ":" + uri.getPort() : "";
            return uri.getScheme() + "://" + uri.getHost() + port;
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }
}
