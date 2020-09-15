package org.ai4bd.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.ai4bd.controller.ImageExtractionController;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.service.ImageExtractionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@WebMvcTest(ImageExtractionController.class)
public class ImageExtractionControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private ImageExtractionService imageExtractionService;

  @Before
  public void init() throws IOException, InterruptedException, ExecutionException {
    ExtractionMessage sample = new ExtractionMessage(UUID.randomUUID(), "InProgress");
    sample.setExtractedByteArray("mock".getBytes());
    BDDMockito.given(
            imageExtractionService.prepareExtractionObject(
                Mockito.anyString(), Mockito.anyString()))
        .willReturn(sample);
    BDDMockito.given(imageExtractionService.processForImage(Mockito.any(), Mockito.any()))
        .willReturn("mock".getBytes());
  }

  @After
  public void cleanUp() {}

  @Test
  public void testPing() throws Exception {
    mockMvc
        .perform(get("/ping"))
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Pong!!"));
  }

  @Test
  public void testExtractImage() throws Exception {
    mockMvc
        .perform(
            post("/extract")
                .content("some data".getBytes())
                .contentType(MediaType.APPLICATION_PDF)
                .header("meta", "Some meta")
                .header("socket-client-id", "Some ids"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("InProgress"));
  }

  @Test
  public void testGetExtractedImageById() throws Exception {
    BDDMockito.given(imageExtractionService.getExtractionObjectById(Mockito.anyString()))
        .willReturn(new ExtractionMessage());
    mockMvc.perform(get("/get/123")).andExpect(status().isNoContent());
  }
}
