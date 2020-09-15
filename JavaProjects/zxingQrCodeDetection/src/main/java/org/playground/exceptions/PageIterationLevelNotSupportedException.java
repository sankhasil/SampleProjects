package org.playground.exceptions;

/**
 * 
 * @author SankyS
 *
 */
public class PageIterationLevelNotSupportedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 2418657241519231969L;

  public PageIterationLevelNotSupportedException() {
    super();
  }

  public PageIterationLevelNotSupportedException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public PageIterationLevelNotSupportedException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public PageIterationLevelNotSupportedException(String arg0) {
    super(arg0);
  }

  public PageIterationLevelNotSupportedException(Throwable arg0) {
    super(arg0);
  }

}
