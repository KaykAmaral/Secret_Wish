package com.example.springApp.config;

import com.example.springApp.security.JwtService;
import com.example.springApp.websocket.StompUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final String frontendOrigin;

    public WebSocketConfig(
            JwtService jwtService,
            @Value("${app.frontend.origin}") String frontendOrigin
    ) {
        this.jwtService = jwtService;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin);
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                String token = resolveToken(accessor);
                Long userId = token == null ? null : jwtService.validateAndGetUserId(token);
                if (userId == null) {
                    throw new IllegalArgumentException("Token JWT invalido para conexao WebSocket");
                }

                accessor.setUser(new StompUserPrincipal(userId.toString()));
                return message;
            }
        });
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        String accessToken = accessor.getFirstNativeHeader("access_token");
        if (accessToken != null && !accessToken.isBlank()) {
            return accessToken;
        }

        return null;
    }
}
