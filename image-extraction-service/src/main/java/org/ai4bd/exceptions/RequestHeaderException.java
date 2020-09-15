/** */
package org.ai4bd.exceptions;

/** @author SankyS */
public class RequestHeaderException extends RuntimeException {

  /** */
  private static final long serialVersionUID = 7371238113083942223L;

  /** */
  public RequestHeaderException() {
    super();
  }

  public RequestHeaderException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public RequestHeaderException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public RequestHeaderException(String arg0) {
    super(arg0);
  }

  public RequestHeaderException(Throwable arg0) {
    super(arg0);
  }
}
