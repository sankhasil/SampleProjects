package org.ai4bd.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;
import org.ai4bd.controller.TesseractController;
import org.ai4bd.service.TesseractService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(TesseractController.class)
public class TesseractControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private TesseractService tesseractService;

  @Before
  public void init() throws JSONException {
    JSONObject resultJsonForDoOcr = new JSONObject().put("text", "It Works");
    JSONArray resultJsonArrayForSegmentationWithTextWordLevel =
        new JSONArray(
            "[{\"bbox"
                + "\":{\"originX\":1384,\"originY\":2119,\"height\":62,\"width\":103},\"content\":\"EUR\","
                + "\"confidence\":94.33981323242188}]");
    // byte[] byteArray = { Mockito.anyByte() };
    BDDMockito /*.after(10001)*/.given(
            tesseractService.doWholePageOcr(Mockito.anyString(), Mockito.anyInt(), Mockito.any(), Mockito.anyBoolean(),Mockito.anyBoolean()))
        .willReturn(CompletableFuture.completedFuture(resultJsonForDoOcr));
    BDDMockito.given(
            tesseractService.processOCRForBoundingBoxWithText(
                Mockito.anyString(), Mockito.any(), Mockito.any()))
        .willReturn(
            CompletableFuture.completedFuture(resultJsonArrayForSegmentationWithTextWordLevel));
  }

  @After
  public void cleanUp() {
    // TODO: add clean up steps
  }

  @Test
  public void testPing() throws Exception {
    mockMvc
        .perform(get("/tesseract/ping"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Pong!!"));
  }

  @Test
  public void testProcessJpgImage() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "eng+deu");
    requestHeaders.add("psm", "3");
    mockMvc
        .perform(
            post("/tesseract/process")
                .contentType("image/jpg")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(MockMvcResultMatchers.content().json("{'text':'It Works'}"));
  }

  @Test
  public void testProcessSegmentationWithText() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "eng+deu");
    requestHeaders.add("psm", "3");
    requestHeaders.add("pageLevel", "3");
    mockMvc
        .perform(
            post("/tesseract/process-for-region-segmentation-with-text")
                .contentType("image/jpg")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    "[{\"bbox\":{\"originX\":1384,"
                        + "\"originY\":2119,\"height\":62,\"width\":103},\"content\":\"EUR\",\"confidence\":94"
                        + ".33981323242188}]"));
  }

  @Test
  public void testNotSupportedLanguageException() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "boot");
    requestHeaders.add("psm", "2");
    mockMvc
        .perform(
            post("/tesseract/process")
                .contentType("image/png")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
        .andExpect(
            MockMvcResultMatchers.content()
                .json("{'error':'boot language code is not " + "Supported to this API'}"));
  }

  @Test
  public void testNotSupportedPSMException() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "eng");
    requestHeaders.add("psm", "21");
    mockMvc
        .perform(
            post("/tesseract/process")
                .contentType("image/jpg")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
        .andExpect(
            MockMvcResultMatchers.content()
                .json("{'error':'21 psm is not Supported to " + "this API'}"));
  }

  @Test
  public void testNotSupportedPageIterationLevelException() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "eng");
    requestHeaders.add("psm", "3");
    requestHeaders.add("pageLevel", "5");
    mockMvc
        .perform(
            post("/tesseract/process-for-region-segmentation-with-text")
                .contentType("image/jpg")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
        .andExpect(
            MockMvcResultMatchers.content()
                .json("{'error':'5 page iteration level is not " + "Supported to this API'}"));
  }

  @Test
  public void testBadRequestException() throws Exception {
    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("lang", "eng");
    requestHeaders.add("psm", "Wrong");
    mockMvc
        .perform(
            post("/tesseract/process")
                .contentType("image/jpg")
                .content("jpg image".getBytes())
                .headers(requestHeaders))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.content().json("{'error':'For input string: \"Wrong\"'}"));
  }

  // TODO: Add more test cases
}
