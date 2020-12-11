package org.ai4bd.exceptions;

/** @author SankyS */
public class ImageProcessingException extends RuntimeException {

  public ImageProcessingException() {
    super();
  }

  public ImageProcessingException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public ImageProcessingException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public ImageProcessingException(String arg0) {
    super(arg0);
  }

  public ImageProcessingException(Throwable arg0) {
    super(arg0);
  }
}
