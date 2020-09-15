/** */
package org.ai4bd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author SankyS */
public class ExtractionRepositoryTest {

  static ExtractionRepository extractionRepository;
  static UUID randomID = UUID.randomUUID();

  @BeforeClass
  public static void init() {
    extractionRepository = new ExtractionRepository();
    extractionRepository.insert(new ExtractionMessage(randomID, "Test"));
  }

  @AfterClass
  public static void cleanUp() {
    extractionRepository.removeAll();
  }

  @Test
  public void testInsert() {
    assertTrue(
        "Data should be succefully inserted",
        extractionRepository.insert(new ExtractionMessage(UUID.randomUUID(), "Test")));
  }

  @Test
  public void testInsertForNull() {
    assertFalse("Data should not get inserted", extractionRepository.insert(null));
  }

  @Test
  public void testFindById() {
    assertNotNull("Should not be null", extractionRepository.findById(randomID));
    assertEquals("Id should be equal", randomID, extractionRepository.findById(randomID).getId());
    assertEquals(
        "status should be equal", "Test", extractionRepository.findById(randomID).getStatus());
  }

  @Test
  public void testUpdate() {
    ExtractionMessage toUpdateObject = extractionRepository.findById(randomID);
    toUpdateObject.setStatus("Updated");
    assertTrue("Data should be succefully updated", extractionRepository.update(toUpdateObject));
    assertEquals(
        "status should be updated and equal",
        "Updated",
        extractionRepository.findById(randomID).getStatus());
  }

  @Test
  public void testRemoveNotPresent() {
    assertFalse("Data should not be deleted", extractionRepository.removeById(UUID.randomUUID()));
  }
}
