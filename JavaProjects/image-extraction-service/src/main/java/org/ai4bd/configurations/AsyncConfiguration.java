package org.ai4bd.configurations;

import java.util.concurrent.Executor;
import org.ai4bd.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfiguration.class);

  @Value("${IMAGE_EXTRACTION_QUEUE_SIZE:10000}")
  private String queueSizeEnv;

  @Value("${NOTIFICATION_STRATEGY: }")
  private String notificationStrategy;

  @Bean(name = "AsyncTaskExecutor")
  public Executor taskExecutor() {
    LOGGER.debug("Creating Async Task Executor");
    CommonConstants.notificationStrategy(notificationStrategy);
    int queueSize;
    try {
      queueSize = Integer.parseInt(queueSizeEnv);
    } catch (NumberFormatException e) {
      LOGGER.debug(
          "Error while parsing \"IMAGE_EXTRACTION_QUEUE_SIZE\" environment variable or Property. Defaulting "
              + "to queue size \"10000\"");
      queueSize = 1000;
    }

    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setQueueCapacity(queueSize);
    executor.initialize();

    return executor;
  }
}
