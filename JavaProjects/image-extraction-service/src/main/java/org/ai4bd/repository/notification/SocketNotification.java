/** */
package org.ai4bd.repository.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.LinkedList;
import java.util.UUID;
import org.ai4bd.model.NotificationMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

/** @author SankyS */
@Repository("socket")
public class SocketNotification implements NotificationStrategy {

  private static final String SOCKET_OUTPUT_PATH = "/output";

  @Value("${communication.protocol.websocketUsage:false}")
  private boolean isSocketUsage;

  @Autowired private SimpMessagingTemplate socketMessagingTemplate;

  private final LinkedList<UUID> connectedUserList = new LinkedList<>();

  private JsonMapper mapper = new JsonMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(SocketNotification.class);

  @EventListener
  public void handleSessionConnectedEvent(SessionConnectedEvent event) {
    if (event != null && event.getUser() != null) {
      this.connectedUserList.add(UUID.fromString(event.getUser().getName()));
    }
  }

  @Override
  public void notify(String destination, NotificationMessage notificationMessage) {
    if (isSocketUsage && notificationMessage != null) {
      try {
        String message = mapper.writeValueAsString(notificationMessage);
        socketMessagingTemplate.convertAndSend(SOCKET_OUTPUT_PATH, message);
        if (StringUtils.isNotBlank(destination)) {
          UUID destinationID = UUID.fromString(destination);
          if (connectedUserList.contains(destinationID)) {
            socketMessagingTemplate.convertAndSendToUser(
                destinationID.toString(), SOCKET_OUTPUT_PATH, message);
          }
        }
      } catch (JsonProcessingException e) {
        LOGGER.error(
            "Json Processing error for {} having meta-info {} with message {}.",
            notificationMessage.getJobId(),
            notificationMessage.getMeta(),
            e.getMessage());
      } catch (IllegalArgumentException ile) {
        LOGGER.error(
            "UUID parsing error for {} with message {}.",
            notificationMessage.getJobId(),
            ile.getMessage());
      }
    }
  }
}
