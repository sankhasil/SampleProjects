/**
 * 
 */
package org.ai4bd.controller.handler;

import org.ai4bd.exceptions.LanguageCodeNotSupportedException;
import org.ai4bd.exceptions.PageIterationLevelNotSupportedException;
import org.ai4bd.exceptions.PageSegmentationCodeNotSupportedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.sourceforge.tess4j.TesseractException;

import java.util.concurrent.ExecutionException;

/**
 * @author SankyS
 *
 */
@ControllerAdvice
public class ApplicationControllerExceptionHandler {

  private static final String ERROR = "error";
  Logger adviceLogger = LoggerFactory.getLogger(ApplicationControllerExceptionHandler.class);

  @ExceptionHandler({TesseractException.class})
  @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
  @ResponseBody
  String exceptionAdviceTesseractHandler(TesseractException te) {
    adviceLogger.error(ExceptionUtils.getStackTrace(te));
    return new JSONObject().put(ERROR, te.getLocalizedMessage()).toString();
  }

  @ExceptionHandler({LanguageCodeNotSupportedException.class,
      PageSegmentationCodeNotSupportedException.class,
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
