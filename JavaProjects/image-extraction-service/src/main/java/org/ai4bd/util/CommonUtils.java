package org.ai4bd.util;

import io.micrometer.core.lang.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ai4bd.exceptions.FolderAcessException;
import org.ai4bd.model.ExtractionMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author SankyS */
public final class CommonUtils {

  private CommonUtils() {}

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

  /**
   * Clean leading "./" or "../" from a file name if necessary.
   *
   * @param fileName File name to clean.
   * @return File name without leading "./" characters.
   */
  public static String cleanFileName(String fileName) {
    if (fileName.startsWith("./") && fileName.length() > 3) {
      fileName = fileName.substring(2);
    } else if (fileName.startsWith("../") && fileName.length() > 4) {
      fileName = fileName.substring(3);
    }
    return fileName;
  }

  /**
   * Extracts the substring after last ".", looks it up in the supported file types and returns the
   * normalized value or null if unsupported.
   *
   * @param fileName File name/path to extract the extension from.
   * @return Normalized extension or null if unsupported.
   */
  @Nullable
  public static String getFileExtension(String fileName) {
    String extension = StringUtils.substringAfterLast(fileName, ".").toLowerCase();
    return CommonConstants.SUPPORTED_FILE_TYPES.get(extension);
  }

  /**
   * Transfers data from a source input stream to a target output stream.
   *
   * @param target Target output stream.
   * @param source Source input stream.
   * @throws IOException On read/write error.
   */
  public static void writeToOutputStream(OutputStream target, InputStream source)
      throws IOException {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = source.read(buffer)) > 0) {
      target.write(buffer, 0, len);
    }
  }

  /**
   * Method to parse number from string which may have * (multiplication sign)
   *
   * @param input {@link String}
   * @return multiplied result as {@link Integer}
   */
  public static int parseNumberForMultiplication(String input) {
    if (StringUtils.isNotBlank(input)) {
      List<String> numberList = Arrays.asList(input.split("\\*"));
      try {
        long result =
            numberList.stream()
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotBlank)
                .mapToLong(Long::parseLong)
                .reduce(Math::multiplyExact)
                .getAsLong();
        if (result < Integer.MAX_VALUE) {
          return (int) result;
        } else {
          return Integer.MAX_VALUE;
        }
      } catch (ArithmeticException ae) {
        return Integer.MAX_VALUE;
      } catch (Exception e) {
        LOGGER.error(ExceptionUtils.getStackTrace(e));
      }
    }

    return 0;
  }

  /**
   * Crates new File with check of directory.
   *
   * @param destinationDir
   * @param zipName
   * @return {@link File}
   * @throws IOException
   */
  public static File newFile(File destinationDir, String zipName) throws IOException {
    File destinationFile = new File(destinationDir, zipName);

    String destinationDirPath = destinationDir.getCanonicalPath();
    String destinationFilePath = destinationFile.getCanonicalPath();

    if (!destinationFilePath.startsWith(destinationDirPath + File.separator)) {
      throw new FolderAcessException("Entry is outside of the target dir: " + zipName);
    }

    return destinationFile;
  }

  /**
   * @param contentType
   * @return {@link String} supported content type as file extension suitable for strategy.
   */
  public static String getSupportedType(String contentType) {
    if (StringUtils.isNotBlank(contentType)) {
      return CommonConstants.SUPPORTED_CONTENT_TYPES
          .getOrDefault(contentType, StringUtils.EMPTY)
          .toLowerCase();
    }
    return StringUtils.EMPTY;
  }

  /**
   * Method to compress list of extracted images into zip archive.
   *
   * @param contentMapToZip {@link Map}
   * @return byte[] compressed zip archive.
   * @throws IOException
   */
  public static byte[] compressToZip(Map<String, byte[]> contentMapToZip) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zipOut = new ZipOutputStream(baos);
    for (String imageName : contentMapToZip.keySet()) {
      ByteArrayInputStream bis = new ByteArrayInputStream(contentMapToZip.get(imageName));
      ZipEntry zipEntry = new ZipEntry(imageName);
      zipOut.putNextEntry(zipEntry);
      final byte[] bytes = new byte[1024];
      int length;
      while ((length = bis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
    }
    zipOut.close();
    baos.close();
    return baos.toByteArray();
  }

  /**
   * Process Meta Info and set into current ExtractionMessage object. The metaInfo is a String
   * initially but is checked if it is a JSONObject or JSONArray or any primitive datatype. If
   * nothing is found or any JsonParsingException occurs then same String is set to metaInfo of
   * ExtractionMessage object.
   *
   * @param initialObject {@link ExtractionMessage} initial object
   * @param metaInfo {@link String} meta information for client.
   * @return {@link ExtractionMessage} initialStepObject
   */
  public static ExtractionMessage processMetaInfoForExtraction(
      ExtractionMessage initialObject, String metaInfo) {
    if (StringUtils.isNotBlank(metaInfo)) {
      try {
        if (StringUtils.startsWith(metaInfo, "{") && StringUtils.endsWith(metaInfo, "}")) {
          initialObject.setMetaInfo(new JSONObject(metaInfo));
          return initialObject;
        }
        if (StringUtils.startsWith(metaInfo, "[") && StringUtils.endsWith(metaInfo, "]")) {
          initialObject.setMetaInfo(new JSONArray(metaInfo));
          return initialObject;
        }
        if (metaInfo.matches("^\\d+(\\.\\d+)?$")) {
          initialObject.setMetaInfo(Double.parseDouble(metaInfo));
          return initialObject;
        }
      } catch (JSONException e) {
        LOGGER.error(metaInfo + " parsing error." + e.getMessage(), e);
      }
      initialObject.setMetaInfo(metaInfo);
    }
    return initialObject;
  }

  /**
   * Retrieves the String representation of JSONObject or JSONArray. If it is primitive then it
   * returns same. This method is helps to Jackson parsing for socket message.
   *
   * @param metaInfo
   * @return {@link Object}
   */
  public static Object getParsedMetaInfo(Object metaInfo) {
    if (metaInfo instanceof JSONObject || metaInfo instanceof JSONArray) {
      return metaInfo.toString();
    }
    return metaInfo;
  }

  /**
   * Gets the complete file path of a document/file.
   *
   * @param fileName
   * @return File path until last "/"
   */
  public static String getFilePath(String fileName) {
    if (StringUtils.containsNone(fileName, '/')) {
      return ".";
    }
    String extension = StringUtils.substringBeforeLast(fileName, "/");
    return StringUtils.isNotBlank(extension) ? extension : ".";
  }
}
