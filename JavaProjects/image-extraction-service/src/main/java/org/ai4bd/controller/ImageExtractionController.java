package org.ai4bd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ai4bd.exceptions.FileTypeNotSupportedException;
import org.ai4bd.exceptions.RequestHeaderException;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.RequestContent;
import org.ai4bd.service.ImageExtractionService;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** @author SankyS */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/")
public class ImageExtractionController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageExtractionController.class);

  @Autowired private ImageExtractionService imageExtractionService;

  /**
   * Health check endpoint for Goss test.
   *
   * @return "Pong!!" {@link String}
   */
  @GetMapping("ping")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public String ping() {
    return "Pong!!";
  }

  /**
   * Endpoint to start the extraction process for any document or archive.
   *
   * @param contentType header param for document mime type.
   * @param metaInfo optional header param
   * @param socketConnectionID optional header param for user specific socket connection.
   * @param content Payload as binary byte[] data representing document or archive
   * @return {@link ExtractionMessage} as response of Job in progress
   * @throws Exception
   */
  @PostMapping(
      value = {"extract", "Extract"},
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = {
        MediaType.APPLICATION_PDF_VALUE,
        "application/zip",
        "application/tar",
        "application/x-tar",
        "image/jpg",
        "image/tiff",
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE,
        "application/x-bzip2",
        "application/gzip"
      })
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ExtractionMessage extractImage(
      @RequestHeader(name = "Content-Type") String contentType,
      @RequestHeader(name = "meta", required = false) String metaInfo,
      @RequestHeader(name = "socket-client-id", required = false) String socketConnectionID,
      @RequestBody() byte[] content)
      throws Exception {
    if (StringUtils.isBlank(contentType)) {
      throw new RequestHeaderException("Content-Type is empty or null");
    }
    String fileType = CommonUtils.getSupportedType(contentType);
    if (StringUtils.isBlank(fileType)) {
      throw new FileTypeNotSupportedException(contentType + " is not supported for extraction.");
    }

    RequestContent requestContent = new RequestContent(fileType, content);

    ExtractionMessage extractionObject =
        imageExtractionService.prepareExtractionObject(metaInfo, socketConnectionID);
    imageExtractionService.processForImage(extractionObject.getId(), requestContent);
    return extractionObject;
  }

  /**
   * Method to retrieve the extracted data as byte[]. If there is any error or partial extraction it
   * responses the as {@link ExtractionMessage} object converted to JSON with partial content
   *
   * @param id {@link String} job extraction id. Usually the UUID generated at the time of
   *     extraction process initiated.
   * @return {@link ResponseEntity} response data as byte[]
   * @throws CloneNotSupportedException
   * @throws JsonProcessingException
   */
  @GetMapping(value = "get/{id}")
  @ResponseBody
  public ResponseEntity<byte[]> getExtractedImageById(@PathVariable(required = true) String id)
      throws CloneNotSupportedException, JsonProcessingException {
    if (StringUtils.isNotBlank(id)) {
      ExtractionMessage resultObject = imageExtractionService.getExtractionObjectById(id);
      if (resultObject != null && resultObject.getId() != null) {
        return processExtractedImage(resultObject);
      }
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.badRequest().build();
  }

  /**
   * Method for processing the response of retrieved object.
   *
   * @param resultObject Retrieved {@link ExtractionMessage} object.
   * @return {@link ResponseEntity<byte[]>} The response data processed.
   * @throws CloneNotSupportedException
   * @throws JsonProcessingException
   */
  private ResponseEntity<byte[]> processExtractedImage(ExtractionMessage resultObject)
      throws CloneNotSupportedException, JsonProcessingException {
    ExtractionMessage outputObject = resultObject.clone();
    if (imageExtractionService.removeExtractionDataById(outputObject.getId())) {
      LOGGER.info("{0} is removed from internal storage.", outputObject.getId());
    }
    ObjectMapper mapper = new ObjectMapper();
    if (outputObject.getStatus().equalsIgnoreCase("done")) {
      if (outputObject.getExtractedByteArray() != null) {
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf(outputObject.getResponseType()))
            .body(outputObject.getExtractedByteArray());
      } else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsBytes(outputObject));
      }
    } else if (outputObject.getFailureReasonMap().size() > 0) {
      if (outputObject.getExtractedByteArray() != null) {
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapper.writeValueAsBytes(outputObject));
      }

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_JSON)
          .body(mapper.writeValueAsBytes(outputObject));
    }
    return ResponseEntity.noContent().build();
  }
}
