/** */
package org.ai4bd.util;

import java.util.List;
import java.util.Map;

/** @author SankyS */
public final class CommonConstants {

  public static final String OUTPUT_EXTENSION = "png";

  public static final List<String> ARCHIVE_FILE_TYPES = List.of("zip", "tar", "7z");

  public static final List<String> COMPRESSED_FILE_TYPES = List.of("bzip2", "gz");

  public static final List<String> DOCUMENT_FILE_TYPES = List.of("png", "jpg", "pdf", "tiff");

  public static final Map<String, String> SUPPORTED_CONTENT_TYPES =
      Map.ofEntries(
          Map.entry("image/png", "png"),
          Map.entry("image/jpg", "jpg"),
          Map.entry("image/jpeg", "jpg"),
          Map.entry("application/pdf", "pdf"),
          Map.entry("application/zip", "zip"),
          Map.entry("application/tar", "tar"),
          Map.entry("application/x-tar", "tar"),
          Map.entry("image/x-tiff", "tiff"),
          Map.entry("image/tiff", "tiff"),
          Map.entry("application/x-bzip2", "bzip2"),
          Map.entry("application/gzip", "gz"));

  public static final Map<String, String> SUPPORTED_FILE_TYPES =
      Map.ofEntries(
          Map.entry("png", "png"),
          Map.entry("jpg", "jpg"),
          Map.entry("jpeg", "jpg"),
          Map.entry("pdf", "pdf"),
          Map.entry("zip", "zip"),
          Map.entry("tar", "tar"),
          Map.entry("tiff", "tiff"),
          Map.entry("tif", "tiff"),
          Map.entry("bzip2", "bzip2"),
          Map.entry("bz2", "bzip2"),
          Map.entry("gz", "gz"),
          Map.entry("tgz", "gz"));

  private static String NOTIFICATION_STRATEGY = "";

  public static final int MAX_ARCHIVE_RECURSION_DEPTH = 3;

  private CommonConstants() {}

  /**
   * Set notification strategy
   *
   * @param nOTIFICATION_STRATEGY
   */
  public static final void notificationStrategy(String nOTIFICATION_STRATEGY) {
    NOTIFICATION_STRATEGY = nOTIFICATION_STRATEGY;
  }

  /**
   * get notification strategy
   *
   * @return
   */
  public static final String notificationStrategy() {
    return NOTIFICATION_STRATEGY;
  }
}
