package org.ai4bd.repository.notification;

import org.ai4bd.model.NotificationMessage;

/** @author SankyS */
public interface NotificationStrategy {

  /**
   * Method to notify success or failure of extraction on document to png image.
   *
   * @param destination {@link String} user specific destination.
   * @param notificationMessage {@link NotificationMessage} message object to be notified.
   */
  void notify(String destination, NotificationMessage notificationMessage);
}
