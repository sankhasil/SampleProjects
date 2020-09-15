package org.ai4bd.controller;

import java.util.concurrent.ExecutionException;
import net.sourceforge.tess4j.TesseractException;
import org.ai4bd.service.TesseractService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AlexN
 * @author SankyS
 */
@RestController
@RequestMapping(value = {"/tesseract", "/Tesseract"})
public class TesseractController {

  Logger controllerLogger = LoggerFactory.getLogger(TesseractController.class);
  TesseractService tesseractService;

  @Autowired
  public TesseractController(TesseractService tesseractService) throws TesseractException {
    this.tesseractService = tesseractService;
    String tessDataLocation = System.getenv("TESSDATA_PREFIX");
    if (StringUtils.isAllBlank(tessDataLocation,tesseractService.getTesseractTrainingDataLocation())) {
      throw new TesseractException("TESSDATA_PREFIX or Tessdata location not configured");
    }else if(StringUtils.isNotBlank(tesseractService.getTesseractTrainingDataLocation())) {
      tesseractService.setTesseractTrainingDataPath(tesseractService.getTesseractTrainingDataLocation());
    }
    
  }

  @GetMapping("/ping")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public String ping() {
    controllerLogger.info("Tessdata location {}",
        tesseractService.getTesseractTrainingDataLocation());
    return "Pong!!";
  }

  @PostMapping(value = "/process", consumes = {"image/png", "image/jpg", "image/tiff"},
      produces = {"application/json"})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String processImage(@RequestHeader(defaultValue = "deu", name = "lang") String lang,
      @RequestHeader(defaultValue = "3", name = "psm") Integer pageSegmentationMode,
      @RequestHeader(defaultValue = "false", name = "deskew") Boolean deskewFlag,
      @RequestHeader(defaultValue = "false", name = "grayScale") Boolean grayScaleFlag,
      @RequestBody() byte[] file) throws ExecutionException, InterruptedException {
    return tesseractService.doWholePageOcr(lang, pageSegmentationMode, file,deskewFlag,grayScaleFlag).get().toString();
  }

  @PostMapping(value = "/process-with-annotations",
      consumes = {"image/png", "image/jpg", "image/tiff"}, produces = {"application/json"})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String processImageWithAnnotations(@RequestHeader("lang") String lang,
      @RequestHeader("psm") Integer pageSegmentationMode,
      @RequestHeader(defaultValue = "false", name = "deskew") Boolean deskewFlag,
      @RequestHeader(defaultValue = "false", name = "grayScale") Boolean grayScaleFlag,
      @RequestHeader("confidence") double confidenceThreshold, @RequestBody() byte[] file)
      throws ExecutionException, InterruptedException {
    return tesseractService
        .doOcrWithWordAnnotations(lang, pageSegmentationMode, confidenceThreshold, file,deskewFlag,grayScaleFlag).get()
        .toString();
  }

  @PostMapping(value = "/process-for-snippet", consumes = {"image/png", "image/jpg", "image/tiff"},
      produces = {"application/json"})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String processImageForSnippet(
      @RequestHeader(defaultValue = "deu", name = "lang") String lang,
      @RequestHeader(defaultValue = "3", name = "psm") Integer pageSegmentationMode,
      @RequestHeader(defaultValue = "0", name = "originX") Integer originX,
      @RequestHeader(defaultValue = "0", name = "originY") Integer originY,
      @RequestHeader(name = "width") Integer width, @RequestHeader(name = "height") Integer height,
      @RequestHeader(defaultValue = "0", name = "deskew") Integer deskew,
      @RequestHeader(defaultValue = "0", name = "grayScale") Integer grayScale,
      @RequestBody() byte[] file) throws ExecutionException, InterruptedException {
    Integer[] configurationArray = {pageSegmentationMode, originX, originY, width, height,deskew,grayScale};
    return tesseractService.doOcrForSnippet(lang, file, configurationArray).get().toString();
  }

  @PostMapping(value = "/process-for-region-segmentation",
      consumes = {"image/png", "image/jpg", "image/tiff"}, produces = {"application/json"})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String processImageForRegionSegmentation(
      @RequestHeader(defaultValue = "deu", name = "lang") String lang,
      @RequestHeader(defaultValue = "3", name = "psm") Integer pageSegmentationMode,
      @RequestHeader(defaultValue = "0", name = "deskew") Integer deskew,
      @RequestHeader(defaultValue = "0", name = "grayScale") Integer grayScale,
      @RequestHeader(name = "pageLevel") Integer pageIterationLevel, @RequestBody() byte[] file)
      throws TesseractException, ExecutionException, InterruptedException {
    Integer[] configurationArray = {pageSegmentationMode, pageIterationLevel,deskew,grayScale};
    return tesseractService.doOcrForRegionSegmentation(lang, file, configurationArray).get()
        .toString();
  }

  @PostMapping(value = "/process-for-region-segmentation-with-text",
      consumes = {"image/png", "image/jpg", "image/tiff"}, produces = {"application/json"})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String processImageForRegionSegmentationWithText(
      @RequestHeader(defaultValue = "deu", name = "lang") String lang,
      @RequestHeader(defaultValue = "3", name = "psm") Integer pageSegmentationMode,
      @RequestHeader(name = "pageLevel") Integer pageIterationLevel,
      @RequestHeader(defaultValue = "0", name = "deskew") Integer deskew,
      @RequestHeader(defaultValue = "0", name = "grayScale") Integer grayScale,
      @RequestBody() byte[] file) throws ExecutionException, InterruptedException {
    Integer[] configurationArray = {pageSegmentationMode, pageIterationLevel, deskew, grayScale};
    return tesseractService.processOCRForBoundingBoxWithText(lang, file, configurationArray).get()
        .toString();
  }
}
