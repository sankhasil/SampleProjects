package org.ai4bd.service.archive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.ai4bd.model.ExtractionMessage;
import org.ai4bd.model.NotificationMessage;
import org.ai4bd.repository.ExtractionRepository;
import org.ai4bd.repository.document.DocumentStrategy;
import org.ai4bd.repository.notification.NotificationStrategy;
import org.ai4bd.util.CommonConstants;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArchiveExtractionService {

  @Autowired protected BeanFactory beanFactory;
  @Autowired protected ExtractionRepository extractionRepository;

  private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveExtractionService.class);

  /**
   * Traverses the provided archive recursively (directories and nested archives) and gathers all
   * found image files with corresponding paths into a map. Decompresses the input if necessary
   * ("tar.gz", "tar.bzip2").
   *
   * @param content Input archive byte code.
   * @param fileType File type of the input archive.
   * @param extractionJobId UUID of the job.
   * @param parentDir Parent directory used in case of recursive calls.
   * @param recursionDepth Recursion depth of the current call.
   * @return Map with image file paths and byte code found inside the supplied archive.
   */
  public Map<String, byte[]> flatten(
      byte[] content, String fileType, UUID extractionJobId, String parentDir, int recursionDepth) {

    Map<String, byte[]> docMap = new HashMap<>();

    // decompress if necessary. it is presumed, that only "tar" archives are compressed.
    if (CommonConstants.COMPRESSED_FILE_TYPES.contains(fileType)) {
      content = decompress(content, fileType, extractionJobId);
      fileType = "tar";
    }

    try (ArchiveInputStream archiveInputStream =
        new ArchiveStreamFactory()
            .createArchiveInputStream(fileType, new ByteArrayInputStream(content))) {

      ArchiveEntry archiveEntry = archiveInputStream.getNextEntry();
      while (archiveEntry != null) {
        if (archiveEntry.isDirectory()) {
          archiveEntry = archiveInputStream.getNextEntry();
          continue;
        }

        String fileExtension = CommonUtils.getFileExtension(archiveEntry.getName());
        if (fileExtension == null) {
          LOGGER.debug(
              "Unsupported file type for file \"{}\" in job ID \"{}\"",
              archiveEntry.getName(),
              extractionJobId);

        } else if ((CommonConstants.COMPRESSED_FILE_TYPES.contains(fileExtension)
                || CommonConstants.ARCHIVE_FILE_TYPES.contains(fileExtension))
            && recursionDepth < CommonConstants.MAX_ARCHIVE_RECURSION_DEPTH) {

          String nestedParentDir = Path.of(parentDir, archiveEntry.getName()).toString();
          docMap.putAll(
              extractArchive(
                  extractionJobId,
                  recursionDepth,
                  archiveInputStream,
                  fileExtension,
                  nestedParentDir));

        } else if (CommonConstants.DOCUMENT_FILE_TYPES.contains(fileExtension)) {
          String fileName =
              Path.of(parentDir, CommonUtils.cleanFileName(archiveEntry.getName())).toString();
          // additional cleaning in case fileName starts with "./"
          fileName = CommonUtils.cleanFileName(fileName);
          byte[] docContent = extractDocumentData(archiveInputStream, extractionJobId);
          if (docContent.length > 0) {
            docMap.put(fileName, docContent);
          }
        }

        archiveEntry = archiveInputStream.getNextEntry();
      }

    } catch (ArchiveException | IOException e) {
      LOGGER.error(
          "Error occurred while handling archive for job ID \"{}\" with message: {}",
          extractionJobId,
          e.getMessage());
    }

    return docMap;
  }

  /**
   * Iterates over the supplied document map calling the corresponding document strategies.
   *
   * @param documentMap Map with image file path and byte code.
   * @param extractionJobId UUID of the current job.
   */
  public void initiateDocumentExtraction(Map<String, byte[]> documentMap, UUID extractionJobId) {
    ExtractionMessage extractionMessage = extractionRepository.findById(extractionJobId);

    if (documentMap == null || documentMap.isEmpty()) {
      NotificationMessage notificationMessage =
          new NotificationMessage()
              .jobId(extractionJobId.toString())
              .meta(CommonUtils.getParsedMetaInfo(extractionMessage.getMetaInfo()));
      beanFactory
          .getBean(CommonConstants.notificationStrategy(), NotificationStrategy.class)
          .notify(extractionMessage.getSocketConnectionID(), notificationMessage);
      return;
    }

    int counter = 0;
    for (Map.Entry<String, byte[]> entry : documentMap.entrySet()) {
      String fileExtension = CommonUtils.getFileExtension(entry.getKey());
      String documentName = entry.getKey().replace(".", "_").replace("/", "_");
      NotificationMessage notificationMessage =
          new NotificationMessage()
              .documentCount(documentMap.size())
              .documentIndex(counter++)
              .documentName(documentName)
              .documentPath(CommonUtils.getFilePath(entry.getKey()))
              .jobId(extractionJobId.toString())
              .meta(CommonUtils.getParsedMetaInfo(extractionMessage.getMetaInfo()));

      if (StringUtils.isBlank(fileExtension)) {
        String entryKey = entry.getKey().replaceAll("[\n\r\t]", "_");
        LOGGER.error(
            "Failed to determine the file type of an archive member for file \"{}\" for job ID \"{}\"",
            entryKey,
            extractionJobId);
        notificationMessage.pageContent(StringUtils.EMPTY);
        beanFactory
            .getBean(CommonConstants.notificationStrategy(), NotificationStrategy.class)
            .notify(extractionMessage.getSocketConnectionID(), notificationMessage);
        continue;
      }

      this.beanFactory
          .getBean(fileExtension, DocumentStrategy.class)
          .extract(entry.getValue(), notificationMessage);
    }
  }

  /**
   * Decompresses supplied content.
   *
   * @param content Byte content of a compressed archive.
   * @param compressionType Type of compression.
   * @return Decompressed archive as byte array.
   */
  private byte[] decompress(byte[] content, String compressionType, UUID extractionJobId) {
    try (CompressorInputStream cis =
        new CompressorStreamFactory()
            .createCompressorInputStream(compressionType, new ByteArrayInputStream(content))) {
      return cis.readAllBytes();
    } catch (CompressorException | IOException e) {
      LOGGER.error("Error occurred while decompressing archive for job ID \"{}\"", extractionJobId);
    }

    return new byte[0];
  }

  /**
   * Helper function to perform a recursive "flatten" call.
   *
   * @param extractionJobId UUID of the job.
   * @param recursionDepth Recursion depth of the current call.
   * @param source Source input stream with archive data.
   * @param fileType File type of the input archive.
   * @param parentDir Parent directory used in case of recursive calls.
   * @return Map with image file paths and byte code found inside the supplied archive.
   */
  private Map<String, byte[]> extractArchive(
      UUID extractionJobId,
      int recursionDepth,
      InputStream source,
      String fileType,
      String parentDir) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      CommonUtils.writeToOutputStream(baos, source);

      return flatten(baos.toByteArray(), fileType, extractionJobId, parentDir, ++recursionDepth);

    } catch (IOException e) {
      LOGGER.warn(
          "Failed to read nested archive for job ID \"{}\" with message: {}",
          extractionJobId,
          e.getMessage());
    }
    return Collections.emptyMap();
  }

  /**
   * Helper function which converts the supplied input stream to a byte array.
   *
   * @param source Source input stream containing document data.
   * @param extractionJobId UUID of the current job (used for logger output).
   * @return Byte array with the documents data.
   */
  private byte[] extractDocumentData(InputStream source, UUID extractionJobId) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      CommonUtils.writeToOutputStream(baos, source);

      return baos.toByteArray();
    } catch (IOException e) {
      LOGGER.warn(
          "Failed to write/read archive part for job ID \"{}\" with message: {}",
          extractionJobId,
          e.getMessage());
    }

    return new byte[0];
  }
}
