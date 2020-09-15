/**
 * 
 */
package org.playground.controller.handler;

import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.playground.exceptions.LanguageCodeNotSupportedException;
import org.playground.exceptions.PageIterationLevelNotSupportedException;
import org.playground.exceptions.XSLTTransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author SankyS
 *
 */
@ControllerAdvice
public class ApplicationControllerExceptionHandler {

  private static final String ERROR = "error";
  Logger adviceLogger = LoggerFactory.getLogger(ApplicationControllerExceptionHandler.class);


  @ExceptionHandler({LanguageCodeNotSupportedException.class,
      XSLTTransformationException.class,
      PageIterationLevelNotSupportedException.class})
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  @ResponseBody
  String exceptionAdviceNotAcceptableHandler(RuntimeException le) {
    adviceLogger.error(ExceptionUtils.getStackTrace(le));
    return new JSONObject().put(ERROR, le.getLocalizedMessage()).toString();
  }

  @ExceptionHandler({NumberFormatException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  String exceptionAdviceBadRequestHandler(RuntimeException e) {
    adviceLogger.error(ExceptionUtils.getStackTrace(e));
    return new JSONObject().put(ERROR, e.getLocalizedMessage()).toString();
  }

  @ExceptionHandler({ExecutionException.class, InterruptedException.class, Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  String exceptionAdviceInternalServerErrorHandler(Exception e) {
    adviceLogger.error(ExceptionUtils.getStackTrace(e));
    return new JSONObject().put(ERROR, e.getLocalizedMessage()).toString();
  }
}
