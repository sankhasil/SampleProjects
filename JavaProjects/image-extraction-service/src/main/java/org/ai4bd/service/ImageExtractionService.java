package org.ai4bd.service;

import java.io.IOException;
import java.util.UUID;
import org.ai4bd.exceptions.ImageProcessingException;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.RequestContent;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** @author SankyS */
@Service
public class ImageExtractionService {

  @Autowired private ExtractionStrategyService extractionStrategyService;

  @Autowired private ExtractionRepository extractionRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageExtractionService.class);

  /**
   * Method to process for image extraction of request content in Asynchronous mode.
   *
   * @param extractionJobId {@link UUID} job extraction id.
   * @param requestContent {@link RequestContent} request object containing data and data type.
   * @return byte[] extracted image png content either single or in compressed zip for multiple
   */
  @Async
  public byte[] processForImage(UUID extractionJobId, RequestContent requestContent) {
    extractionStrategyService.process(requestContent, extractionJobId);
    ExtractionMessage currentObject = extractionRepository.findById(extractionJobId);
    if (currentObject.getExtractedByteMap() != null) {
      currentObject.setStatus("Done");
      if (currentObject.getExtractedByteMap().size() > 1) {
        try {
          currentObject.setResponseType("application/zip");
          currentObject.setExtractedByteArray(
              CommonUtils.compressToZip(currentObject.getExtractedByteMap()));
        } catch (IOException e) {
          throw new ImageProcessingException("Error in  compressing to Zip.", e);
        }
      } else if (currentObject.getExtractedByteMap().size() == 1) {
        currentObject.setResponseType(MediaType.IMAGE_PNG_VALUE);
        currentObject.setExtractedByteArray(
            currentObject.getExtractedByteMap().values().iterator().next());
      }
    }
    extractionRepository.update(currentObject);
    return currentObject.getExtractedByteArray();
  }

  /**
   * Method to retrieve Extraction Message Object by unique id from in-memory store.
   *
   * @param id {@link String} uuid as string.
   * @return {@link ExtractionMessage} extraction object retrieved from in-memory store.
   */
  public ExtractionMessage getExtractionObjectById(String id) {
    try {
      UUID uid = UUID.fromString(id);
      return extractionRepository.findById(uid);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(id + " not an UUID", e);
    }
  }

  /**
   * Method to remove the extraction object from in-memory store.
   *
   * @param id {@link UUID} unique id.
   * @return true if successful removal else false.
   */
  public boolean removeExtractionDataById(UUID id) {
    return extractionRepository.removeById(id);
  }

  /**
   * Method to prepare Extraction Message object for process Image Extraction.
   *
   * @param metaInfo {@link String} Meta information for client user.
   * @param socketConnectionID {@link String} optional connection id for socket based notification.
   * @return {@link ExtractionMessage}
   */
  public ExtractionMessage prepareExtractionObject(String metaInfo, String socketConnectionID) {
    UUID generatedId = UUID.randomUUID();
    ExtractionMessage initialStepObject = new ExtractionMessage(generatedId, "InProgress");
    if (StringUtils.isNotBlank(socketConnectionID)) {
      initialStepObject.setSocketConnectionID(socketConnectionID);
    }

    extractionRepository.insert(
        CommonUtils.processMetaInfoForExtraction(initialStepObject, metaInfo));

    return initialStepObject;
  }
}
