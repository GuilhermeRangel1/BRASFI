package com.brasfi.webapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig  implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topics");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/brasfi-webapp-websocket")
                .setAllowedOriginPatterns("*")  // Permite todas as origens (ajuste para produção)
                .withSockJS()  // Habilita fallback SockJS
                .setHeartbeatTime(25000)  // Intervalo de heartbeat (ms)
                .setDisconnectDelay(5000);  // Atraso para desconexão
    }
}
