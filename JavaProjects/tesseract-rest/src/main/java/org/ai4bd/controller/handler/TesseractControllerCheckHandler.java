/**
 * 
 */
package org.ai4bd.controller.handler;

import javax.servlet.http.HttpServletRequest;

import org.ai4bd.exceptions.LanguageCodeNotSupportedException;
import org.ai4bd.exceptions.PageIterationLevelNotSupportedException;
import org.ai4bd.exceptions.PageSegmentationCodeNotSupportedException;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author SankyS
 *
 */
@ControllerAdvice
@Order(1)
public class TesseractControllerCheckHandler {

  @InitBinder
  public void test(HttpServletRequest request) {
    String lang = request.getHeader("lang");
    String psm = request.getHeader("psm");
    String pageIterationLevel = request.getHeader("pageLevel");

    if (StringUtils.isNotBlank(lang) && !CommonUtils.checkIfLanguageCodesExists(lang)) {
      throw new LanguageCodeNotSupportedException(
          lang + " language code is not Supported to this API");
    }
    if (StringUtils.isNotBlank(psm)
        && !CommonUtils.checkIfPageSegmentationModeExists(Integer.valueOf(psm))) {
      throw new PageSegmentationCodeNotSupportedException(
          psm + " psm is not Supported to this API");
    }
    if (StringUtils.isNotBlank(pageIterationLevel)
        && !CommonUtils.checkIfPageIterationLevelExists(Integer.valueOf(pageIterationLevel))) {
      throw new PageIterationLevelNotSupportedException(
          pageIterationLevel + " page iteration level is not Supported to this API");
    }
  }
}
