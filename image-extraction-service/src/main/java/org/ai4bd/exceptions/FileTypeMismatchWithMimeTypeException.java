/**
 * 
 */
package org.ai4bd.exceptions;

/**
 * @author SankyS
 *
 */
public class FileTypeMismatchWithMimeTypeException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 627548243700918801L;

  public FileTypeMismatchWithMimeTypeException() {
    super();
  }

  public FileTypeMismatchWithMimeTypeException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public FileTypeMismatchWithMimeTypeException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public FileTypeMismatchWithMimeTypeException(String arg0) {
    super(arg0);
  }

  public FileTypeMismatchWithMimeTypeException(Throwable arg0) {
    super(arg0);
  }
}
