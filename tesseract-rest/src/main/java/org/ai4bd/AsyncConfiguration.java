package org.ai4bd;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfiguration.class);

  @Value("${TESSREST_QUEUE_SIZE:1000}")
      private String queueSizeEnv;

  @Bean(name = "AsyncTaskExecutor")
  public Executor taskExecutor() {
    LOGGER.debug("Creating Async Task Executor");

    int queueSize;
    try {
      queueSize = Integer.parseInt(queueSizeEnv);
    } catch (NumberFormatException e) {
      LOGGER.debug("Error while parsing \"TESSREST_QUEUE_SIZE\" environment variable. Defaulting " +
          "to queue size \"1000\"");
      queueSize = 1000;
    }

    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setQueueCapacity(queueSize);
    executor.initialize();

    return executor;
  }
}
