package com.trackingpath.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;

import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer { // ✅ FIX: added interface

    @Value("${app.ws.allowed-origin-patterns}")
    private List<String> allowedOriginPatterns;
    @Autowired
    private AuthChannelInterceptor authChannelInterceptor;
    // ================= SECURITY =================
    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager() {

        return AuthorizationManagers.anyOf(
            (authentication, message) -> {
                // allow CONNECT / HEARTBEAT / DISCONNECT
                SimpMessageType type =
                    SimpMessageHeaderAccessor.getMessageType(message.getHeaders());

                if (type == SimpMessageType.CONNECT ||
                    type == SimpMessageType.HEARTBEAT ||
                    type == SimpMessageType.DISCONNECT) {
                    return new AuthorizationDecision(true);
                }

                return new AuthorizationDecision(
                    authentication.get().isAuthenticated()
                );
            }
        );
    }

    // ================= WEBSOCKET =================
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor); // 🔥 THIS IS REQUIRED
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setHandshakeHandler(new WebSocketPrincipalHandshakeHandler())
                .setAllowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .withSockJS();
    }
}