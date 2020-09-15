package org.ai4bd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.util.CommonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.MediaType;

/** @author SankyS */
public class CommonUtilsTest {

  private static final String JSON_DATA_SHORT =
      "{\"annotationToUse\":\"\",\"title\":\"sdakgoij\",\"itemType\":\"edm:DocumentPage\",\"ocrBasedAnnotation\":\"false\",\"collectionSize\":\"0\",\"userId\":\"adam.grabowski\"}";
  private static final String JSON_DATA_LONG =
      "{\"annotationToUse\":\"http://ai4bd.com/resource/data/collection-annotation-type/ha232789-g72s-5a12-952c-826372c4f981,http://ai4bd.com/resource/data/collection-annotation-type/eq982789-g72s-8r23-952c-926372d1g321,http://ai4bd.com/resource/data/collection-annotation-type/lo297789-k11s-8r23-953d-936572d1j244\",\"title\":\"asldjgnsgn\",\"itemType\":\"edm:DocumentPage\",\"ocrBasedAnnotation\":\"false\",\"collectionSize\":\"0\",\"userId\":\"adam.grabowski\"}";
  private static final String JSON_DATA_WRONG = "{\"test\":\"test content\",}";

  private static final String JSON_DATA_OBJECT = "{'test':'test value'}";
  private static final String JSON_DATA_ARRAY = "[{'test':'test value'},{'test2':'test value2'}]";

  @Test
  public void testBasicParseNumberForMultipication() {
    int result = CommonUtils.parseNumberForMultiplication("10 * 12");
    assertEquals("The Result should be 120", 120, result);
  }

  @Test
  public void testNegativeParseNumberForMultipication() {
    int result = CommonUtils.parseNumberForMultiplication("10 * -12");
    assertEquals("The Result should be 120", -120, result);
  }

  @Test
  public void testWrongStringParseNumberForMultipication() {
    int result = CommonUtils.parseNumberForMultiplication("12SF * -12");
    assertEquals("The Result should be 0", 0, result);
  }

  @Test
  public void testSingleNumberParseNumberForMultipication() {
    int result = CommonUtils.parseNumberForMultiplication("112");
    assertEquals("The Result should be 112", 112, result);
  }

  @Test
  public void testOutofBoundParseNumberForMultipication() {
    int result = CommonUtils.parseNumberForMultiplication("1024 * 1024 * 1024 * 1024");
    assertEquals("The Result should be 2147483647", Integer.MAX_VALUE, result);
  }

  @Test
  public void testNewFile() throws IOException {
    assertNotNull(CommonUtils.newFile(new File("temp"), "SomeFile"));
  }

  @Test
  public void testGetSupportedTypePDF() {
    assertEquals(
        "Provides the type from content-type",
        "pdf",
        CommonUtils.getSupportedType(MediaType.APPLICATION_PDF_VALUE));
  }

  @Test
  public void testGetSupportedTypeNotSupported() {
    assertEquals(
        "Provides empty string for not supported content-type",
        "",
        CommonUtils.getSupportedType(MediaType.APPLICATION_JSON_VALUE));
  }

  @Test
  public void testPrepareExtractionObjectForMetaJsonDataShort() throws JSONException {
    ExtractionMessage messageObject =
        CommonUtils.processMetaInfoForExtraction(new ExtractionMessage(), JSON_DATA_SHORT);
    assertEquals(
        "Meta Info should be same ",
        new JSONObject(JSON_DATA_SHORT).toString(),
        messageObject.getMetaInfo().toString());
  }

  @Test
  public void testPrepareExtractionObjectForMetaJsonDataLong() throws JSONException {
    ExtractionMessage messageObject =
        CommonUtils.processMetaInfoForExtraction(new ExtractionMessage(), JSON_DATA_LONG);
    assertEquals(
        "Meta Info should be same ",
        new JSONObject(JSON_DATA_LONG).toString(),
        messageObject.getMetaInfo().toString());
  }

  @Test
  public void testPrepareExtractionObjectForMetaJsonDataWrong() {
    ExtractionMessage messageObject =
        CommonUtils.processMetaInfoForExtraction(new ExtractionMessage(), JSON_DATA_WRONG);
    assertEquals(
        "Meta Info should be same ", JSON_DATA_WRONG, messageObject.getMetaInfo().toString());
  }

  @Test
  public void testGetParsedMetaInfoForJSONObject() throws JSONException {
    assertEquals(
        "Meta Info should be parsed and same ",
        new JSONObject(JSON_DATA_OBJECT).toString(),
        CommonUtils.getParsedMetaInfo(new JSONObject(JSON_DATA_OBJECT)).toString());
  }

  @Test
  public void testGetParsedMetaInfoForJSONArray() throws JSONException {
    assertEquals(
        "Meta Info should be parsed and same ",
        new JSONArray(JSON_DATA_ARRAY).toString(),
        CommonUtils.getParsedMetaInfo(new JSONArray(JSON_DATA_ARRAY)).toString());
  }

  @Test
  public void testGetParsedMetaInfoForPrimitiveInteger() throws JSONException {
    assertEquals(
        "Meta Info should be parsed and same ", 12354, CommonUtils.getParsedMetaInfo(12354));
  }

  @Test
  public void testGetParsedMetaInfoForPrimitiveDouble() throws JSONException {
    assertEquals(
        "Meta Info should be parsed and same ", 1235.0d, CommonUtils.getParsedMetaInfo(1235.0d));
  }

  @Test
  public void testGetFileExtensionPng() {
    assertEquals(
        "The file extension should be same",
        "png",
        CommonUtils.getFileExtension("temp/folderOne/one.png"));
  }

  @Test
  public void testGetFileExtensionTiff() {
    assertEquals(
        "The file extension should be same",
        "tiff",
        CommonUtils.getFileExtension("temp/folderOne/two.tif"));
  }

  @Test
  public void testGetFileExtensionEmpty() {
    assertEquals(
        "The file extension should be same", null, CommonUtils.getFileExtension("unknown.unknown"));
  }

  @Test
  public void testGetFilePath() {
    assertEquals(
        "The file path should be same",
        "temp/folderOne",
        CommonUtils.getFilePath("temp/folderOne/one.png"));
  }

  @Test
  public void testGetFilePathEmpty() {
    assertEquals("The file path should be same", ".", CommonUtils.getFilePath(""));
  }

  @Test
  public void testGetFilePathSingleFile() {
    assertEquals("The file path should be same", ".", CommonUtils.getFilePath("test.pdf"));
  }

  @Test
  public void testGetFilePathSingleFolder() {
    assertEquals(
        "The file path should be same",
        "SingleFolder",
        CommonUtils.getFilePath("SingleFolder/test.pdf"));
  }
}
