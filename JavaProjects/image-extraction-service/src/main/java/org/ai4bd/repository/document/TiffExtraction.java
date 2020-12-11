package org.ai4bd.repository.document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.repository.notification.NotificationStrategy;
import org.ai4bd.util.CommonConstants;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;

@Repository("tiff")
public class TiffExtraction implements DocumentStrategy {

  @Autowired private BeanFactory beanFactory;
  @Autowired private ExtractionRepository extractionRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(TiffExtraction.class);

  @Override
  public void extract(byte[] content, NotificationMessage notificationMessageObject) {
    NotificationStrategy notifyBean =
        beanFactory.getBean(CommonConstants.notificationStrategy(), NotificationStrategy.class);
    ExtractionMessage currentObject =
        extractionRepository.findById(notificationMessageObject.getJobId());
    notificationMessageObject.meta(CommonUtils.getParsedMetaInfo(currentObject.getMetaInfo()));

    try {
      List<BufferedImage> bufferedImageList = Imaging.getAllBufferedImages(content);
      LOGGER.info("{} conversion starts ...", notificationMessageObject.getDocumentName());
      for (int i = 0; i < bufferedImageList.size(); i++) {
        try {
          String imageFileName =
              notificationMessageObject.getDocumentName()
                  + "_"
                  + i
                  + "."
                  + CommonConstants.OUTPUT_EXTENSION;
          BufferedImage image = bufferedImageList.get(i);
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          ImageIO.write(image, CommonConstants.OUTPUT_EXTENSION.toUpperCase(), os);
          notificationMessageObject
              .pageFileName(imageFileName)
              .pageCount(bufferedImageList.size())
              .pageIndex(i)
              .pageWidth(image.getWidth())
              .pageHeight(image.getHeight())
              .pageContent(Base64Utils.encodeToString(os.toByteArray()));

          currentObject.addExtractedByte(imageFileName, os.toByteArray());
          notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
        } catch (Exception e) {
          // sends message to socket for possible errors
          notificationMessageObject
              .pageContent(StringUtils.EMPTY)
              .documentCount(1)
              .documentIndex(0);
          notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
        }
      }
      LOGGER.info(
          "{} conversion ends... for {} page(s) ! ",
          notificationMessageObject.getDocumentName(),
          bufferedImageList.size());
      extractionRepository.update(currentObject);
    } catch (Exception e) {
      notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
      currentObject.addFailureReason("ERROR_IN_TIFF_TO_PNG_CONVERSION", e.getMessage());
      extractionRepository.update(currentObject);
      LOGGER.error(
          "{} not processed. Error: {}", notificationMessageObject.getJobId(), e.getMessage());
    }
  }
}
