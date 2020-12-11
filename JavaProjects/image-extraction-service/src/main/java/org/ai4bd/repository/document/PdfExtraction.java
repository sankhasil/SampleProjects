package org.ai4bd.repository.document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.repository.notification.NotificationStrategy;
import org.ai4bd.util.CommonConstants;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;

@Repository("pdf")
public class PdfExtraction implements DocumentStrategy {

  private static final int IMAGE_DPI = 200;
  @Autowired BeanFactory beanFactory;
  @Autowired ExtractionRepository extractionRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfExtraction.class);

  @Override
  public void extract(byte[] content, NotificationMessage notificationMessageObject) {
    NotificationStrategy notifyBean =
        beanFactory.getBean(CommonConstants.notificationStrategy(), NotificationStrategy.class);
    ExtractionMessage currentObject =
        extractionRepository.findById(notificationMessageObject.getJobId());
    notificationMessageObject.meta(CommonUtils.getParsedMetaInfo(currentObject.getMetaInfo()));

    try (PDDocument document = PDDocument.load(content)) {
      PDFRenderer renderer = new PDFRenderer(document);
      LOGGER.info("{} conversion starts ...", notificationMessageObject.getDocumentName());
      for (int i = 0; i < document.getNumberOfPages(); i++) {
        try {
          String imageFileName =
              notificationMessageObject.getDocumentName()
                  + "_"
                  + i
                  + "."
                  + CommonConstants.OUTPUT_EXTENSION;
          // 200 is sample dots per inch.
          // if necessary, change 200 into another integer.
          BufferedImage image = renderer.renderImageWithDPI(i, IMAGE_DPI);
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          ImageIO.write(image, CommonConstants.OUTPUT_EXTENSION.toUpperCase(), os);
          notificationMessageObject
              .pageFileName(imageFileName)
              .pageCount(document.getNumberOfPages())
              .pageIndex(i)
              .pageWidth(image.getWidth())
              .pageHeight(image.getHeight())
              .pageContent(Base64Utils.encodeToString(os.toByteArray()));

          currentObject.addExtractedByte(imageFileName, os.toByteArray());

          notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
        } catch (Exception e) {
          LOGGER.error("Exception in pdf to image conversion {}", e.getMessage());
          notificationMessageObject.pageContent(StringUtils.EMPTY);
          notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
        }
      }
      LOGGER.info(
          "{} conversion ends... for {} page(s) ! ",
          notificationMessageObject.getDocumentName(),
          document.getNumberOfPages());
      extractionRepository.update(currentObject);
    } catch (IOException e) {

      notifyBean.notify(currentObject.getSocketConnectionID(), notificationMessageObject);
      LOGGER.error(
          "{} not processed. Error: {}", notificationMessageObject.getJobId(), e.getMessage());
    }
  }
}
