package org.ai4bd.service;

import java.util.Map;
import java.util.UUID;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.model.RequestContent;
import org.ai4bd.repository.document.DocumentStrategy;
import org.ai4bd.service.archive.ArchiveExtractionService;
import org.ai4bd.util.CommonConstants;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtractionStrategyService {

  @Autowired private BeanFactory beanFactory;
  @Autowired private ArchiveExtractionService archiveExtractionService;

  /**
   * Method to initiate the process of extraction based on request data content type.
   *
   * @param requestContent {@link RequestContent} request data to be processed.
   * @param jobId {@link UUID} extraction job ID
   */
  public void process(RequestContent requestContent, UUID jobId) {
    if (CommonConstants.ARCHIVE_FILE_TYPES.contains(requestContent.getContentType())
        || CommonConstants.COMPRESSED_FILE_TYPES.contains(requestContent.getContentType())) {
      Map<String, byte[]> documentMap =
          archiveExtractionService.flatten(
              requestContent.getContent(), requestContent.getContentType(), jobId, "", 0);
      archiveExtractionService.initiateDocumentExtraction(documentMap, jobId);

    } else if (CommonConstants.DOCUMENT_FILE_TYPES.contains(requestContent.getContentType())) {
      NotificationMessage notificationMessageObject =
          new NotificationMessage()
              .jobId(jobId.toString())
              .documentCount(1)
              .documentIndex(0)
              .documentName("temp_" + jobId.toString() + "_" + requestContent.getContentType())
              .documentPath(".");
      beanFactory
          .getBean(requestContent.getContentType(), DocumentStrategy.class)
          .extract(requestContent.getContent(), notificationMessageObject);
    }
  }
}
