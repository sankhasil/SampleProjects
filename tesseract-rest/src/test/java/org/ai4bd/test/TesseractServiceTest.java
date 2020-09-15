package org.ai4bd.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.sourceforge.tess4j.TesseractException;
import org.ai4bd.service.TesseractService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
@ActiveProfiles("test")
public class TesseractServiceTest {

  private static final int PAGE_SEGMENTATION_MODE = 3;
  private static final String LANGUAGE_CODE = "eng+deu";

  @Autowired
  TesseractService tesseractService;
  static byte[] imageBytes;

  @BeforeClass
  public static void init() throws IOException {
    imageBytes = Files.readAllBytes(new File("test.png").toPath());
  }

  @AfterClass
  public static void cleanUp() {
    // TODO: destroy or clean up steps
  }

  @Test
  public void testDoOcr()
      throws JSONException, InterruptedException, ExecutionException, TimeoutException {
    JSONObject resultJson =
        tesseractService.doWholePageOcr(LANGUAGE_CODE, PAGE_SEGMENTATION_MODE, imageBytes,false,false).get();
    assertTrue("Should have content length more than 0", resultJson.getString("text").length() > 0);
    assertTrue("Should have content with 'United States' word",
        resultJson.getString("text").contains("United States"));
  }

  @Test
  public void testDoOcrWithWordAnnotation()
      throws JSONException, InterruptedException, ExecutionException, TimeoutException {
    JSONObject resultJson = tesseractService
        .doOcrWithWordAnnotations(LANGUAGE_CODE, PAGE_SEGMENTATION_MODE, 0.1, imageBytes,false,false).get();
    assertTrue("Should have content length more than 0", resultJson.getString("text").length() > 0);
    assertTrue("Should have one or more annotations",
        resultJson.getJSONArray("annotations").length() > 0);
  }

  @Test
  public void testDoOcrWithWordAnnotationWithNonMatchingWords()
      throws JSONException, InterruptedException, ExecutionException, IOException {
    byte[] imageBytesFail =
        Files.readAllBytes(new File("src/test/resources/tess-fail.png").toPath());
    JSONObject resultJson = tesseractService
        .doOcrWithWordAnnotations(LANGUAGE_CODE, PAGE_SEGMENTATION_MODE, 0.678, imageBytesFail,false,false)
        .get();
    assertTrue("Should have content length more than 0", resultJson.getString("text").length() > 0);
    assertTrue("Should have one or more annotations",
        resultJson.getJSONArray("annotations").length() > 0);
  }

  @Test
  public void testDoOcrForSnippet_no_configuration()
      throws JSONException, InterruptedException, ExecutionException, TimeoutException {
    Integer[] zero_configuration = {};
    JSONObject resultJson =
        tesseractService.doOcrForSnippet(LANGUAGE_CODE, imageBytes, zero_configuration).get();
    assertTrue("Should have content length more than 0", resultJson.getString("text").length() > 0);
    assertTrue("Should have content with 'United States' word",
        resultJson.getString("text").contains("United States"));
  }

  @Test
  public void testDoOcrForSnippet_only_snippet_configuration()
      throws JSONException, InterruptedException, ExecutionException, TimeoutException {
    Integer[] only_snippet_configuration = {0, 0, 900, 600};
    JSONObject resultJson = tesseractService
        .doOcrForSnippet(LANGUAGE_CODE, imageBytes, only_snippet_configuration).get();
    assertTrue("Should have content length more than 0", resultJson.getString("text").length() > 0);
    assertTrue("Should have content with 'United States' word",
        resultJson.getString("text").contains("United States"));
  }

  @Test
  public void testDoOcrForRegionSegmentation()
      throws TesseractException, InterruptedException, ExecutionException, TimeoutException {
    Integer[] configuration = {3, 1};
    JSONArray resultJsonArray =
        tesseractService.doOcrForRegionSegmentation(LANGUAGE_CODE, imageBytes, configuration).get();
    assertTrue("Should have content length more than 0", resultJsonArray.length() > 0);
  }

  @Test
  public void testDoOcrForRegionSegmentationWithText()
      throws InterruptedException, ExecutionException, TimeoutException {
    Integer[] configuration = {3, 1};
    JSONArray resultJsonArray = tesseractService
        .processOCRForBoundingBoxWithText(LANGUAGE_CODE, imageBytes, configuration).get();
    assertTrue("Should have content length more than 0", resultJsonArray.length() > 0);
    assertTrue("Should have content some text and bounding boxes",
        resultJsonArray.optJSONObject(11).optString("content").length() > 0
            && resultJsonArray.optJSONObject(11).optJSONObject("bbox").optInt("originX") > -1);
  }
  // TODO: add more test cases
}
