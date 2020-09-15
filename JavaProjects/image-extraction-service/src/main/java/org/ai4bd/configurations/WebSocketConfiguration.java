package org.ai4bd.configurations;

import java.util.List;
import org.ai4bd.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/** @author SankyS */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
  @Value("${WEB_SOCKET_SEND_BUFFER_LIMIT: 1024 * 1024 * 1024}")
  private String sendBufferSizeLimit;

  @Value("${WEB_SOCKET_MESSAGE_LIMIT: 1024 * 1024}")
  private String messageSizeLimit;

  @Value("${WEB_SOCKET_SEND_TIME_LIMIT: 60 * 1000}")
  private String sendTimeLimit;

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfiguration.class);

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic/reciever", "/output");
    config.setApplicationDestinationPrefixes("/app");
    config.setUserRegistryOrder(1);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/extract-image")
        .setAllowedOrigins("*")
        .setHandshakeHandler(new UserBasedCustomHandshakeHandler());
    registry
        .addEndpoint("/extract-image")
        .setAllowedOrigins("*")
        .setHandshakeHandler(new UserBasedCustomHandshakeHandler())
        .withSockJS();
  }

  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    int parsedBufferSizeLimit = CommonUtils.parseNumberForMultiplication(sendBufferSizeLimit);
    int parsedMessageSizeLimit = CommonUtils.parseNumberForMultiplication(messageSizeLimit);
    int parsedSendTimeLimit = CommonUtils.parseNumberForMultiplication(sendTimeLimit);
    registration.setMessageSizeLimit(
        parsedMessageSizeLimit == 0
            ? CommonUtils.parseNumberForMultiplication("1024*1024")
            : parsedMessageSizeLimit);
    registration.setSendBufferSizeLimit(
        parsedBufferSizeLimit == 0
            ? CommonUtils.parseNumberForMultiplication("1024*1024*1024")
            : parsedBufferSizeLimit);
    registration.setSendTimeLimit(
        parsedSendTimeLimit == 0
            ? CommonUtils.parseNumberForMultiplication("60*10000")
            : parsedSendTimeLimit);

    LOGGER.info(
        "Message size limit is: {}", CommonUtils.parseNumberForMultiplication(messageSizeLimit));
    LOGGER.info(
        "Send buffer size is: {}", CommonUtils.parseNumberForMultiplication(sendBufferSizeLimit));
    LOGGER.info("Send time limit is: {}", CommonUtils.parseNumberForMultiplication(sendTimeLimit));
  }

  @Override
  public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
    messageConverters.add(new ByteArrayMessageConverter());
    return false;
  }
}
