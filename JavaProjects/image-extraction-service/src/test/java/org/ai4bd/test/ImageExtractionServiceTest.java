package org.ai4bd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.RequestContent;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.service.ImageExtractionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/** @author SankyS */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ImageExtractionServiceTest {

  private static UUID generatedID;
  @Autowired ImageExtractionService imageExtractionService;
  @Autowired ExtractionRepository extractionRepository;
  private static byte[] fileBytes;

  @BeforeClass
  public static void init() {
    generatedID = UUID.randomUUID();
  }

  @AfterClass
  public static void cleanUp() {}

  @Test
  public void testProcessForImageInPdf() throws IOException {
    fileBytes = Files.readAllBytes(new File("src/test/resources/documents/test.pdf").toPath());
    ExtractionMessage testMessageObject = new ExtractionMessage(generatedID, "In-Progress");
    extractionRepository.insert(testMessageObject);
    RequestContent requestContent = new RequestContent("pdf", fileBytes);
    imageExtractionService.processForImage(generatedID, requestContent);
    assertEquals(
        "File content size should be same",
        314142,
        extractionRepository.findById(generatedID).getExtractedByteArray().length);
  }

  @Test
  public void testProcessForImageInJpg() throws IOException {
    fileBytes = Files.readAllBytes(new File("src/test/resources/documents/test.jpg").toPath());
    ExtractionMessage testMessageObject = new ExtractionMessage(generatedID, "In-Progress");
    extractionRepository.insert(testMessageObject);
    RequestContent requestContent = new RequestContent("jpg", fileBytes);
    imageExtractionService.processForImage(generatedID, requestContent);
    assertEquals(
        "File content size should be same",
        303096,
        extractionRepository.findById(generatedID).getExtractedByteArray().length);
  }

  @Test
  public void testPrepareExtractionObject() {
    ExtractionMessage messageObject =
        imageExtractionService.prepareExtractionObject("test info", null);
    assertNotEquals("Unique ID should not be same ", generatedID, messageObject.getId());
    assertEquals("Meta Info should be same ", "test info", messageObject.getMetaInfo());
    assertEquals("Initial Status should be same ", "InProgress", messageObject.getStatus());
  }
}
