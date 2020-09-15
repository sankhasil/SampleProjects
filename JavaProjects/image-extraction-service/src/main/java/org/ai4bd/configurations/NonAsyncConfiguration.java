package org.ai4bd.configurations;

import java.util.concurrent.Executor;
import org.ai4bd.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class NonAsyncConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(NonAsyncConfiguration.class);

  @Value("${NOTIFICATION_STRATEGY: }")
  private String notificationStrategy;

  @Bean(name = "NonAsyncTaskExecutor")
  public Executor taskExecutor() {
    LOGGER.debug("Creating non-Async Task Executor");
    CommonConstants.notificationStrategy(notificationStrategy);
    return null;
  }
}
