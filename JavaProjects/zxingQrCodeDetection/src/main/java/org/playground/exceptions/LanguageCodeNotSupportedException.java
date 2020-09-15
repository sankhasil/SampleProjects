package org.playground.exceptions;

/**
 * 
 * @author SankyS
 *
 */
public class LanguageCodeNotSupportedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 2418657241519231969L;

  public LanguageCodeNotSupportedException() {
    super();
  }

  public LanguageCodeNotSupportedException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public LanguageCodeNotSupportedException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public LanguageCodeNotSupportedException(String arg0) {
    super(arg0);
  }

  public LanguageCodeNotSupportedException(Throwable arg0) {
    super(arg0);
  }

}
