package org.ai4bd.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.service.archive.ArchiveExtractionService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ArchiveExtractionServiceTest {

  private static final String ARCHIVE_MAP_KEY = "010_B201140132.png";

  static UUID generatedID;

  @Autowired ExtractionRepository extractionRepository;
  @Autowired ArchiveExtractionService archiveExtractionService;

  private static final String TEST_ARCHIVE_FOLDER = "src/test/resources/archives/";

  @BeforeClass
  public static void init() {
    generatedID = UUID.randomUUID();
  }

  @Test
  public void testSimpleZip() throws IOException {
    // arrange
    byte[] fileBytes = Files.readAllBytes(new File(TEST_ARCHIVE_FOLDER + "test_pdf.zip").toPath());
    // act
    Map<String, byte[]> flattenedArchive =
        archiveExtractionService.flatten(fileBytes, "zip", generatedID, "", 0);
    // assert
    Assert.assertEquals("Unzipped number of file(s) should be same.", 1, flattenedArchive.size());
    Assert.assertTrue(
        "Unzipped file map should have test.pdf file.", flattenedArchive.containsKey("test.pdf"));
    Assert.assertNotNull("File content should not be null.", flattenedArchive.get("test.pdf"));
    Assert.assertTrue(
        "File content length shoulbe be more than 0.", flattenedArchive.get("test.pdf").length > 0);
  }

  @Test
  public void testSimple7z() throws IOException {
    // arrange
    byte[] fileBytes =
        Files.readAllBytes(new File(TEST_ARCHIVE_FOLDER + "test_tif_blub.7z").toPath());
    // act
    Map<String, byte[]> flattenedArchive =
        archiveExtractionService.flatten(fileBytes, "7z", generatedID, "", 0);
    Assert.assertEquals("Unzipped number of file(s) should be same.", 0, flattenedArchive.size());
  }

  @Test
  public void testComplexTar() throws IOException {
    // arrange
    byte[] fileBytes = Files.readAllBytes(new File(TEST_ARCHIVE_FOLDER + "tar.tar").toPath());
    // act
    Map<String, byte[]> flattenedArchive =
        archiveExtractionService.flatten(fileBytes, "tar", generatedID, "", 0);
    // assert
    Assert.assertEquals("Un-tarred number of file(s) should be same.", 6, flattenedArchive.size());
    Assert.assertTrue(
        "Un-tarred file map should have 010_B201140132.png file.",
        flattenedArchive.containsKey(ARCHIVE_MAP_KEY));
    Assert.assertNotNull("File content should not be null.", flattenedArchive.get(ARCHIVE_MAP_KEY));
    Assert.assertTrue(
        "File content length shoulbe be more than 0.",
        flattenedArchive.get(ARCHIVE_MAP_KEY).length > 0);
  }

  @Test
  public void testComplexZip() throws IOException {
    // arrange
    byte[] fileBytes = Files.readAllBytes(new File(TEST_ARCHIVE_FOLDER + "zip.zip").toPath());
    // act
    Map<String, byte[]> flattenedArchive =
        archiveExtractionService.flatten(fileBytes, "zip", generatedID, "", 0);
    // assert
    Assert.assertEquals("Unzipped number of file(s) should be same.", 7, flattenedArchive.size());
    Assert.assertTrue(
        "Unzipped file map should have bugs_everywhere.png file.",
        flattenedArchive.containsKey("bugs_everywhere.png"));
    Assert.assertNotNull(
        "File content should not be null.", flattenedArchive.get("bugs_everywhere.png"));
    Assert.assertTrue(
        "File content length shoulbe be more than 0.",
        flattenedArchive.get("bugs_everywhere.png").length > 0);
  }
}
