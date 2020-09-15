/** */
package org.ai4bd.repository.notification;

import com.hazelcast.core.HazelcastInstance;
import org.ai4bd.model.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** @author SankyS */
public class HazelCastNotification implements NotificationStrategy {

  @Value("${connection.hazelcast.map:image-extraction}")
  private String hazelcastMapName;

  @Value("${communication.protocol.hazelcastUsage:false}")
  private boolean isHazelcastUsage;

  @Autowired(required = false)
  public HazelcastInstance hazelcastInstance;

  @Override
  public void notify(String destination, NotificationMessage notificationMessage) {
    if (isHazelcastUsage && notificationMessage != null) {
      hazelcastInstance
          .getMap(hazelcastMapName)
          .put(notificationMessage.getJobId(), notificationMessage);
    }
  }
}
