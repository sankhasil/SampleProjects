/**
 * 
 */
package org.ai4bd.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author SankyS
 *
 */
@Configuration
public class HazelcastConfiguration {

  @Value("${communication.protocol.hazelcastUsage:false}")
  private boolean hazelcastUsage;

  @Value("${connection.hazelcast.remote.address:127.0.0.1:5701,127.0.0.1:5701}")
  private String hazelcastNetworkAddressList;


  @Bean
  @ConditionalOnProperty(prefix = "communication.protocol", name = "hazelcastUsage")
  public ClientConfig clientConfig() {
    ClientConfig clientConfig = new ClientConfig();
      ClientNetworkConfig networkConfig = clientConfig.getNetworkConfig();
      networkConfig.addAddress(hazelcastNetworkAddressList.split(",")).setSmartRouting(true)
          .addOutboundPortDefinition("34700-34710").setRedoOperation(true)
          .setConnectionTimeout(5000).setConnectionAttemptLimit(5);

    return clientConfig;

  }

  @Bean
  @ConditionalOnProperty(prefix = "communication.protocol", name = "hazelcastUsage")
  public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
      return HazelcastClient.newHazelcastClient(clientConfig);
  }
}
