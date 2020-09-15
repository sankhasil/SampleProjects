package org.ai4bd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.repository.document.JpgExtraction;
import org.ai4bd.repository.document.PdfExtraction;
import org.ai4bd.repository.document.PngExtraction;
import org.ai4bd.repository.document.TiffExtraction;
import org.ai4bd.repository.notification.NotificationStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DocumentExtractionStrategyTest {

  static UUID generatedID;
  static NotificationMessage notificationMessage;
  private static final String TEST_DATA_FOLDER = "src/test/resources/documents/";
  @Autowired ExtractionRepository extractionRepository;

  @MockBean NotificationStrategy notificationStrategy;

  @Autowired BeanFactory beanFactory;

  @BeforeClass
  public static void init() {
    generatedID = UUID.randomUUID();
    notificationMessage =
        new NotificationMessage().documentCount(1).documentIndex(0).jobId(generatedID.toString());
  }

  @Before
  public void setup() {
    extractionRepository.insert(new ExtractionMessage(generatedID, "Test"));
    BDDMockito.doNothing().when(notificationStrategy).notify(Mockito.anyString(), Mockito.any());
  }

  @After
  public void cleanup() {
    extractionRepository.removeAll();
  }

  @Test
  public void testJpgExtraction() throws IOException {
    byte[] fileBytes = Files.readAllBytes(new File(TEST_DATA_FOLDER + "test.jpg").toPath());
    notificationMessage.documentName("test_jpg");
    beanFactory.getBean(JpgExtraction.class).extract(fileBytes, notificationMessage);
    Map<String, byte[]> extractedDataMap =
        extractionRepository.findById(generatedID).getExtractedByteMap();
    assertNotNull("Extracted data map should not be null", extractedDataMap);
    assertEquals("Size of extracted data map should be 1", 1, extractedDataMap.size());
    assertEquals(
        "Extracted byte length should be equal",
        303096,
        extractedDataMap.values().iterator().next().length);
    assertTrue(
        "Extracted fileName should be equal",
        "test_jpg.png".equalsIgnoreCase(extractedDataMap.keySet().iterator().next()));
  }

  @Test
  public void testPngExtraction() throws IOException {
    byte[] fileBytes = Files.readAllBytes(new File(TEST_DATA_FOLDER + "test.png").toPath());
    notificationMessage.documentName("test_png");
    beanFactory.getBean(PngExtraction.class).extract(fileBytes, notificationMessage);
    Map<String, byte[]> extractedDataMap =
        extractionRepository.findById(generatedID).getExtractedByteMap();
    assertNotNull("Extracted data map should not be null", extractedDataMap);
    assertEquals("Size of extracted data map should be 1", 1, extractedDataMap.size());
    assertEquals(
        "Extracted byte length should be equal",
        52474,
        extractedDataMap.values().iterator().next().length);
    assertTrue(
        "Extracted fileName should be equal",
        "test_png.png".equalsIgnoreCase(extractedDataMap.keySet().iterator().next()));
  }

  @Test
  public void testTiffExtraction() throws IOException {
    byte[] fileBytes = Files.readAllBytes(new File(TEST_DATA_FOLDER + "test.tif").toPath());
    notificationMessage.documentName("test_tiff");
    beanFactory.getBean(TiffExtraction.class).extract(fileBytes, notificationMessage);
    Map<String, byte[]> extractedDataMap =
        extractionRepository.findById(generatedID).getExtractedByteMap();
    assertNotNull("Extracted data map should not be null", extractedDataMap);
    assertEquals("Size of extracted data map should be 2", 2, extractedDataMap.size());
    assertEquals(
        "Extracted byte length should be equal",
        69358,
        extractedDataMap.values().iterator().next().length);
    assertTrue(
        "Extracted fileName should be equal",
        "test_tiff_0.png".equalsIgnoreCase(extractedDataMap.keySet().iterator().next()));
  }

  @Test
  public void testPdfExtraction() throws IOException {
    byte[] fileBytes = Files.readAllBytes(new File(TEST_DATA_FOLDER + "test.pdf").toPath());
    notificationMessage.documentName("test_pdf");
    beanFactory.getBean(PdfExtraction.class).extract(fileBytes, notificationMessage);
    Map<String, byte[]> extractedDataMap =
        extractionRepository.findById(generatedID).getExtractedByteMap();
    assertNotNull("Extracted data map should not be null", extractedDataMap);
    assertEquals("Size of extracted data map should be 1", 1, extractedDataMap.size());
    assertEquals(
        "Extracted byte length should be equal",
        26844,
        extractedDataMap.values().iterator().next().length);
    assertTrue(
        "Extracted fileName should be equal",
        "test_pdf_0.png".equalsIgnoreCase(extractedDataMap.keySet().iterator().next()));
  }
}
