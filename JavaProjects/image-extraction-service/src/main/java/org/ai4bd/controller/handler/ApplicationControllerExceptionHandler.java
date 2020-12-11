package org.ai4bd.controller.handler;

import java.util.concurrent.ExecutionException;
import org.ai4bd.exceptions.FileTypeMismatchWithMimeTypeException;
import org.ai4bd.exceptions.FileTypeNotSupportedException;
import org.ai4bd.exceptions.FolderAcessException;
import org.ai4bd.exceptions.ImageProcessingException;
import org.ai4bd.exceptions.RequestHeaderException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** @author SankyS */
@ControllerAdvice
public class ApplicationControllerExceptionHandler {

  private static final String ERROR = "error";
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ApplicationControllerExceptionHandler.class);

  @ExceptionHandler({
    NumberFormatException.class,
    MethodArgumentTypeMismatchException.class,
    FileTypeNotSupportedException.class,
    FileTypeMismatchWithMimeTypeException.class,
    RequestHeaderException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  String exceptionAdviceBadRequestHandler(RuntimeException e) {
    LOGGER.error(ExceptionUtils.getStackTrace(e));
    return new JSONObject().put(ERROR, e.getLocalizedMessage()).toString();
  }

  @ExceptionHandler({FolderAcessException.class, ImageProcessingException.class})
  @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
  @ResponseBody
  String exceptionAdvicePreConditionFailedHandler(RuntimeException e) {
    LOGGER.error(ExceptionUtils.getStackTrace(e));
    return new JSONObject().put(ERROR, e.getLocalizedMessage()).toString();
  }

  @ExceptionHandler({
    ExecutionException.class,
    InterruptedException.class,
    CloneNotSupportedException.class,
    Exception.class
  })
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  String exceptionAdviceInternalServerErrorHandler(Exception e) {
    LOGGER.error(ExceptionUtils.getStackTrace(e));
    return new JSONObject().put(ERROR, e.getLocalizedMessage()).toString();
  }
}
