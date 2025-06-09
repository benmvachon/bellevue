package com.village.bellevue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.village.bellevue.config.security.AMBHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

  @Value("${spring.rabbitmq.host}")
  private String rabbitMqHost;

  @Value("${spring.rabbitmq.stomp.port:61613}")
  private int rabbitMqStompPort;

  @Value("${spring.rabbitmq.username}")
  private String rabbitMqUsername;

  @Value("${spring.rabbitmq.password}")
  private String rabbitMqPassword;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
      .setHandshakeHandler(new AMBHandshakeHandler())
      .setAllowedOriginPatterns("*") // Use more secure origin filtering in production
      .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");

    registry.enableStompBrokerRelay("/topic", "/queue")
      .setRelayHost(rabbitMqHost)
      .setRelayPort(rabbitMqStompPort)
      .setClientLogin(rabbitMqUsername)
      .setClientPasscode(rabbitMqPassword)
      .setSystemLogin(rabbitMqUsername)
      .setSystemPasscode(rabbitMqPassword)
      .setSystemHeartbeatSendInterval(10000)
      .setSystemHeartbeatReceiveInterval(10000);
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new LoggingChannelInterceptor());
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    registration.interceptors(new LoggingChannelInterceptor());
  }

  public static class LoggingChannelInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
      log.debug("WebSocket message: {}", message);
      return message;
    }
  }
}
