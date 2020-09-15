package org.playground.exceptions;

/**
 * 
 * @author SankyS
 *
 */
public class XSLTTransformationException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 2418657241519231969L;

  public XSLTTransformationException() {
    super();
  }

  public XSLTTransformationException(String arg0, Throwable arg1, boolean arg2,
      boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public XSLTTransformationException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public XSLTTransformationException(String arg0) {
    super(arg0);
  }

  public XSLTTransformationException(Throwable arg0) {
    super(arg0);
  }

}
