package org.ai4bd.exceptions;

/**
 * 
 * @author SankyS
 *
 */
public class FileTypeNotSupportedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 2418657241519231969L;

  public FileTypeNotSupportedException() {
    super();
  }

  public FileTypeNotSupportedException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public FileTypeNotSupportedException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public FileTypeNotSupportedException(String arg0) {
    super(arg0);
  }

  public FileTypeNotSupportedException(Throwable arg0) {
    super(arg0);
  }

}
