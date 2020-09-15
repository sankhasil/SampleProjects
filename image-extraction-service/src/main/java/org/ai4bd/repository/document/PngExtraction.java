package org.ai4bd.repository.document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.repository.notification.NotificationStrategy;
import org.ai4bd.util.CommonConstants;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;

@Repository("png")
public class PngExtraction implements DocumentStrategy {

  @Autowired private BeanFactory beanFactory;
  @Autowired private ExtractionRepository extractionRepository;
  private static final Logger LOGGER = LoggerFactory.getLogger(PngExtraction.class);

  @Override
  public void extract(byte[] content, NotificationMessage notificationMessageObject) {
    NotificationStrategy notifyBean =
        beanFactory.getBean(CommonConstants.notificationStrategy(), NotificationStrategy.class);
    ExtractionMessage currentObject =
        extractionRepository.findById(notificationMessageObject.getJobId());
    notificationMessageObject.meta(CommonUtils.getParsedMetaInfo(currentObject.getMetaInfo()));
    try {
      BufferedImage imagePng = ImageIO.read(new ByteArrayInputStream(content));
      String fileName =
          notificationMessageObject.getDocumentName() + "." + CommonConstants.OUTPUT_EXTENSION;
      notificationMessageObject
          .pageContent(Base64Utils.encodeToString(content))
          .pageCount(1)
          .pageIndex(0)
          .pageFileName(fileName)
          .pageHeight(imagePng.getHeight())
          .pageWidth(imagePng.getWidth());

      notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
      currentObject.addExtractedByte(fileName, content);
      extractionRepository.update(currentObject);
    } catch (IOException ioe) {

      notificationMessageObject.pageContent(StringUtils.EMPTY);
      notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);

      LOGGER.error("Error in Png processing. message {}", ioe.getMessage());
      currentObject.addFailureReason("ERROR_IN_PNG_TO_PNG_CONVERSION", ioe.getMessage());
      extractionRepository.update(currentObject);
    }
  }
}
