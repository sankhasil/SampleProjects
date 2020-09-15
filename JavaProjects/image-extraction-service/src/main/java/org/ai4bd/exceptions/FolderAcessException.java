package org.ai4bd.exceptions;

/**
 * 
 * @author SankyS
 *
 */
public class FolderAcessException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 2418657241519231969L;

  public FolderAcessException() {
    super();
  }

  public FolderAcessException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public FolderAcessException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public FolderAcessException(String arg0) {
    super(arg0);
  }

  public FolderAcessException(Throwable arg0) {
    super(arg0);
  }

}
