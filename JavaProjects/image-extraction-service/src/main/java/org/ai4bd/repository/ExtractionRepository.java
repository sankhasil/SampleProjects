/** */
package org.ai4bd.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.ai4bd.model.ExtractionMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/** @author SankyS */
@Repository
public class ExtractionRepository {

  private final Map<UUID, ExtractionMessage> extractionMessageStore = new HashMap<>();

  /**
   * Insert {@link ExtractionMessage} object into Map store.
   *
   * @param object {@link ExtractionMessage}
   * @return true or false if data is added or not.
   */
  public boolean insert(ExtractionMessage object) {
    if (object != null
        && object.getId() != null
        && !extractionMessageStore.containsKey(object.getId())) {
      extractionMessageStore.put(object.getId(), object);
      return true;
    }
    return false;
  }

  /**
   * Update {@link ExtractionMessage} object in Map store.
   *
   * @param object {@link ExtractionMessage}
   * @return true or false if data is updated or not.
   */
  public boolean update(ExtractionMessage object) {
    if (object != null
        && object.getId() != null
        && extractionMessageStore.containsKey(object.getId())) {
      extractionMessageStore.replace(object.getId(), object);
      return true;
    }
    return false;
  }

  /**
   * Remove {@link ExtractionMessage} object from Map store.
   *
   * @param id {@link String}
   * @return true or false if data is removed or not.
   */
  public boolean removeById(String id) {
    if (StringUtils.isNotBlank(id)) {
      UUID extractionID = UUID.fromString(id);
      return removeById(extractionID);
    }
    return false;
  }

  /**
   * Remove {@link ExtractionMessage} object from Map store.
   *
   * @param id {@link UUID}
   * @return true or false if data is removed or not.
   */
  public boolean removeById(UUID id) {
    if (id != null && extractionMessageStore.containsKey(id)) {
      extractionMessageStore.remove(id);
      return true;
    }
    return false;
  }

  /**
   * Retrieves the {@link ExtractionMessage} object from Map store.
   *
   * @param id {@link String}
   * @return {@link ExtractionMessage} object
   */
  public ExtractionMessage findById(String id) {
    if (StringUtils.isNotBlank(id)) {
      UUID extractionID = UUID.fromString(id);
      return findById(extractionID);
    }
    return null;
  }

  /**
   * Retrieves the {@link ExtractionMessage} object from Map store.
   *
   * @param id {@link String}
   * @return {@link ExtractionMessage} object
   */
  public ExtractionMessage findById(UUID id) {
    if (id != null && extractionMessageStore.containsKey(id)) {
      return extractionMessageStore.get(id);
    }
    return null;
  }

  /**
   * Retrieves all the {@link ExtractionMessage} object from Map store.
   *
   * @return {@link List<ExtractionMessage>} as List
   */
  public List<ExtractionMessage> findAll() {
    if (extractionMessageStore.size() > 0) {
      return List.copyOf(extractionMessageStore.values());
    }
    return Collections.emptyList();
  }

  public void removeAll() {
    extractionMessageStore.clear();
  }
}
