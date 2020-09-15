package org.ai4bd.repository.document;

import org.ai4bd.model.NotificationMessage;

public interface DocumentStrategy {
  /**
   * Method to extract png image(s) from byte[] content provided.
   *
   * @param content byte content
   * @param notificationMessage {@link NotificationMessage}
   */
  void extract(byte[] content, NotificationMessage notificationMessage);
}
