package org.ai4bd.service;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.ImageHelper;
import org.ai4bd.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author AlexN
 * @author SankyS
 */
@Service
public class TesseractService {


  private static final String JSON_KEY_CONTENT = "content";
  private static final String JSON_KEY_CONFIDENCE = "confidence";
  private static final String JSON_KEY_WIDTH = "width";
  private static final String JSON_KEY_HEIGHT = "height";
  private static final String JSON_KEY_ORIGIN_Y = "originY";
  private static final String JSON_KEY_ORIGIN_X = "originX";
  public static final String IMAGE_IS_NULL = "Image is null";
  private static TesseractService instance = null;
  private Tesseract tesseract = new Tesseract();
  Logger serviceLogger = LoggerFactory.getLogger(TesseractService.class);

  @Value("${tessdata.location: }")
  private String tesseractTrainingDataLocation;

  public static TesseractService getInstance() {
    if (instance == null) {
      instance = new TesseractService();
    }
    return instance;
  }

  public void setTesseractTrainingDataPath(String tessdataPath) {
    tesseract.setDatapath(tessdataPath);
  }
  private JSONObject doOcr(String lang, int pageSegmentationMode, BufferedImage img, boolean deskewFlag, boolean grayScaleFlag)
      throws TesseractException {
    tesseract.setLanguage(CommonUtils.convertLanguageCodeToThreeLetterCode(lang));
    tesseract.setPageSegMode(pageSegmentationMode);
    img = preProcessImage(img, deskewFlag, grayScaleFlag);
    return new JSONObject().put("text", tesseract.doOCR(img));
  }

  /**
   * Process whole page for OCR
   *
   * @param lang as {@link String}
   * @param pageSegmentationMode as {@link Integer}
   * @param file as {@link Byte}[]
   * @return text recognized in {@link JSONObject}
   */
  @Async
  public CompletableFuture<JSONObject> doWholePageOcr(String lang, int pageSegmentationMode,
      byte[] file,boolean deskewFlag, boolean grayScaleFlag) {
    try {
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(file));
      return CompletableFuture.completedFuture(doOcr(lang, pageSegmentationMode, img,deskewFlag,grayScaleFlag));
    } catch (IOException | TesseractException e) {
      return CompletableFuture.completedFuture(new JSONObject().put("text", StringUtils.EMPTY));
    }
  }

  /**
   * Process Text and Annotations using OCR
   *
   * @param lang as {@link String}
   * @param pageSegmentationMode as {@link Integer}
   * @param confidenceThreshold as {@link Double}
   * @param file as {@link Byte}[]
   * @return text recognized in {@link JSONObject} and annotations
   */
  @Async
  public CompletableFuture<JSONObject> doOcrWithWordAnnotations(String lang,
      int pageSegmentationMode, double confidenceThreshold, byte[] file,boolean deskewFlag, boolean grayScaleFlag) {
    BufferedImage img;
    JSONObject ocrResult;
    double confidenceThresholdInPercent = confidenceThreshold * 100;

    try {
      img = ImageIO.read(new ByteArrayInputStream(file));
      ocrResult = doOcr(lang, pageSegmentationMode, img,deskewFlag,grayScaleFlag);
    } catch (IOException | TesseractException e) {
      return CompletableFuture.completedFuture(new JSONObject().put("text", StringUtils.EMPTY));
    }
    String ocrText = ocrResult.optString("text");

    if (StringUtils.isBlank(ocrText)) {
      return CompletableFuture.completedFuture(ocrResult);
    }

    List<Word> words = tesseract.getWords(img, ITessAPI.TessPageIteratorLevel.RIL_WORD);
    words.removeIf(word -> word.getText().matches("[\\s]+"));

    JSONArray annotations = new JSONArray();

    getWordAnnotations(confidenceThresholdInPercent, ocrText, words, annotations);
    return CompletableFuture.completedFuture(ocrResult.put("annotations", annotations));
  }

  /**
   * Get words below confidence threshold and create annotations.
   *
   * @param confidenceThresholdInPercent as {@link Double}
   * @param ocrText as {@link String}
   * @param words as {@link List}
   * @param annotations as {@link JSONArray}
   */
  private void getWordAnnotations(double confidenceThresholdInPercent, String ocrText,
      List<Word> words, JSONArray annotations) {

    for (int i = 0; i < words.size(); i++) {
      double confidence = words.get(i).getConfidence();
      // no need to annotate all words, but only those with a low confidence level
      if (confidence < confidenceThresholdInPercent) {

        createAnnotationFromWord(ocrText, words, annotations, i, confidence);
      }
    }
  }

  /**
   * Process the portion of image and use OCR
   *
   * @param lang as {@link String}
   * @param file as {@link Byte}[]
   * @param configurations as {@link Integer}[]
   * @return text recognized in {@link JSONObject}
   */
  @Async
  public CompletableFuture<JSONObject> doOcrForSnippet(String lang, byte[] file,
      Integer... configurations) {
    Rectangle snippetRectangle;
    BufferedImage image;
    try {
      image = ImageIO.read(new ByteArrayInputStream(file));
    } catch (IOException e) {
      serviceLogger.error(IMAGE_IS_NULL, e);
      return null;
    }
    boolean deskewFlag = false;
    boolean grayScaleFlag = false;
    switch (configurations.length) {
      case 7:
        tesseract.setPageSegMode(configurations[0]);
        snippetRectangle = new Rectangle(configurations[1], configurations[2], configurations[3],
            configurations[4]);
        deskewFlag = configurations[5] > 0 ? true : false;
        grayScaleFlag = configurations[6] > 0 ? true : false;
        break;
      case 6:
        tesseract.setPageSegMode(configurations[0]);
        snippetRectangle = new Rectangle(configurations[1], configurations[2], configurations[3],
            configurations[4]);
        deskewFlag = configurations[5] > 0 ? true : false;
        break;
      case 5:
        tesseract.setPageSegMode(configurations[0]);
        snippetRectangle = new Rectangle(configurations[1], configurations[2], configurations[3],
            configurations[4]);
        break;
      case 4:
        tesseract.setPageSegMode(6);
        snippetRectangle = new Rectangle(configurations[0], configurations[1], configurations[2],
            configurations[3]);
        break;
      default:
        tesseract.setPageSegMode(6);
        snippetRectangle = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        break;
    }
    String ocrData = StringUtils.EMPTY;
    try {
      tesseract.setLanguage(CommonUtils.convertLanguageCodeToThreeLetterCode(lang));
      image = preProcessImage(image, deskewFlag, grayScaleFlag);
      ocrData = tesseract.doOCR(image, snippetRectangle);
    } catch (TesseractException e) {
      // Do Nothing
      serviceLogger.error(ExceptionUtils.getStackTrace(e));
    }
    return CompletableFuture.completedFuture(new JSONObject().put("text", ocrData));
  }


  /** @return tesseract training data location as {@link String} */
  public String getTesseractTrainingDataLocation() {
    if (StringUtils.isBlank(tesseractTrainingDataLocation)) {
      tesseractTrainingDataLocation = System.getenv("TESSDATA_PREFIX");
    }
    return tesseractTrainingDataLocation;
  }

  /**
   * process image to create Region segmentations in a JSON Array
   *
   * @param lang as {@link String}
   * @param file as {@link Byte}[]
   * @param configurations as {@link Integer}[]
   * @return segmentation in {@link JSONArray}
   */
  @Async
  public CompletableFuture<JSONArray> doOcrForRegionSegmentation(String lang, byte[] file,
      Integer[] configurations) throws TesseractException {
    JSONArray boundingBoxArray = new JSONArray();
    BufferedImage image;
    try {
      image = extractImageWithPreProcessing(file, configurations);
    } catch (IOException e) {
      serviceLogger.error(IMAGE_IS_NULL, e);
      return null;
    }
    int pageIteratorLevel = processTesseractConfiguration(lang, configurations);
    tesseract.getSegmentedRegions(image, pageIteratorLevel).parallelStream()
        .map(rectangleMapper -> {
          JSONObject boudingBox = new JSONObject();
          boudingBox.put(JSON_KEY_ORIGIN_X, rectangleMapper.getX());
          boudingBox.put(JSON_KEY_ORIGIN_Y, rectangleMapper.getY());
          boudingBox.put(JSON_KEY_HEIGHT, rectangleMapper.getHeight());
          boudingBox.put(JSON_KEY_WIDTH, rectangleMapper.getWidth());
          return boudingBox;
        }).forEach(boundingBoxArray::put);
    return CompletableFuture.completedFuture(boundingBoxArray);
  }



  /**
   * Process the image for bounding boxes with text and confidence.
   *
   * @param lang as {@link String}
   * @param file as {@link Byte}[]
   * @param configurations as {@link Integer}
   * @return bounding boxes with text and confidence in {@link JSONArray}
   */
  @Async
  public CompletableFuture<JSONArray> processOCRForBoundingBoxWithText(String lang, byte[] file,
      Integer[] configurations) {
    BufferedImage image;
    try {
      image = extractImageWithPreProcessing(file, configurations);
    } catch (IOException e) {
      serviceLogger.error(IMAGE_IS_NULL, e);
      return null;
    }

    int pageIterationLevel = processTesseractConfiguration(lang, configurations);
    return CompletableFuture.completedFuture(new JSONArray(
        CommonUtils.prettifyWords(tesseract.getWords(image, pageIterationLevel)).parallelStream()
            .filter(Objects::nonNull).map(this::getResultJsonObject).collect(Collectors.toList())));
  }

  /**
   * 
   * @param file
   * @param configurations
   * @return
   * @throws IOException
   */
  private BufferedImage extractImageWithPreProcessing(byte[] file, Integer[] configurations)
      throws IOException {
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(file));
    boolean deskewFlag = false;
    boolean grayScaleFlag = false;
    if (configurations.length == 3) {
      deskewFlag = configurations[2] > 0 ? true : false;
    }
    if (configurations.length == 4) {
      deskewFlag = configurations[2] > 0 ? true : false;
      grayScaleFlag = configurations[3] > 0 ? true : false;
    }
    image = preProcessImage(image, deskewFlag, grayScaleFlag);
    return image;
  }
  /**
   * 
   * @param image
   * @param deskewFlag
   * @param grayScaleFlag
   * @return
   */
  private BufferedImage preProcessImage(BufferedImage image, boolean deskewFlag,
      boolean grayScaleFlag) {
    if (grayScaleFlag)
      image = ImageHelper.convertImageToGrayscale(image);
    if (deskewFlag)
      image = CommonUtils.deskewImage(image, 0.05d);
    return image;
  }
  /**
   * Create annotation from word
   *
   * @param ocrText as {@link String}
   * @param words as {@link List}
   * @param annotations as {@link JSONArray}
   * @param i as {@link Integer}
   * @param confidence as {@link Double}
   */
  private void createAnnotationFromWord(String ocrText, List<Word> words, JSONArray annotations,
      int i, double confidence) {

    String wordText = words.get(i).getText();

    // the words need need to be found in ocr String (whole picture ocr). that's why
    // colons need to be removed as before with ocr.
    // as only the leading/tailing colons are removed, only the 1st and the last
    // words need to be checked for colons
    if (i == 0) {
      wordText = CommonUtils.leadingColonRemoval(wordText);
    }
    if (i == words.size() - 1) {
      wordText = CommonUtils.tailingColonRemoval(wordText);
    }

    if (StringUtils.isBlank(wordText)) {
      return;
    }

    // look for the word in the whole picture ocr to determine the position
    List<MatchResult> matchList = getMatchResults(ocrText, wordText);

    if (matchList.isEmpty()) {
      if (serviceLogger.isWarnEnabled()) {
        serviceLogger.warn(String.format("Failed to find annotation \"%s\" in ocr text: \"%s\"",
            wordText, ocrText));
      }
      return;
    }

    // if there are multiple matches for the word, we just take the last matches
    // confidence level
    int counter = getLastMatch(words, i, wordText, matchList);
    MatchResult matchResult = matchList.get(counter);
    JSONObject annotationsObj = parseOcrAnnotation(matchResult, confidence);
    annotations.put(annotationsObj);
  }

  private JSONObject getResultJsonObject(Word word) {
    JSONObject boundingBox = new JSONObject();
    boundingBox.put(JSON_KEY_ORIGIN_X, word.getBoundingBox().getX());
    boundingBox.put(JSON_KEY_ORIGIN_Y, word.getBoundingBox().getY());
    boundingBox.put(JSON_KEY_HEIGHT, word.getBoundingBox().getHeight());
    boundingBox.put(JSON_KEY_WIDTH, word.getBoundingBox().getWidth());
    JSONObject resultJsonObject = new JSONObject();
    resultJsonObject.put("bbox", boundingBox);
    resultJsonObject.put(JSON_KEY_CONTENT, word.getText());
    resultJsonObject.put(JSON_KEY_CONFIDENCE, word.getConfidence());
    return resultJsonObject;
  }

  private int processTesseractConfiguration(String lang, Integer[] configurations) {
    int pageIteratorLevel = 3;
    if (configurations.length == 2) {
      tesseract.setPageSegMode(configurations[0]);
      pageIteratorLevel = configurations[1];
    } else {
      tesseract.setPageSegMode(6);
    }
    tesseract.setLanguage(CommonUtils.convertLanguageCodeToThreeLetterCode(lang));
    return pageIteratorLevel;
  }

  private int getLastMatch(List<Word> words, int i, String text, List<MatchResult> matchList) {
    int counter = 0;
    if (matchList.size() > 1) {
      for (int j = 0; j < i; j++) {
        counter += StringUtils.countMatches(words.get(j).getText(), text);
      }
      if (counter != 0) {
        counter--;
      }
    }
    return counter;
  }

  private List<MatchResult> getMatchResults(String ocr, String text) {
    Matcher matcher =
        Pattern.compile(String.format("(%s)", Pattern.quote(text)), Pattern.DOTALL).matcher(ocr);
    List<MatchResult> matchList = new ArrayList<>();
    while (matcher.find()) {
      matchList.add(matcher.toMatchResult());
    }
    return matchList;
  }

  /**
   * Creates a json object representing the edm:OcrAnnotation.
   *
   * @param matchResult MatchResult object with the text value.
   * @param confidence The confidence value (0..100)
   * @return Json object representing the edm:OcrAnnotation.
   */
  private JSONObject parseOcrAnnotation(MatchResult matchResult, double confidence) {

    return new JSONObject().put(JSON_KEY_CONFIDENCE, String.valueOf(confidence / 100))
        .put(JSON_KEY_CONTENT, matchResult.group())
        .put("annotationStart", String.valueOf(matchResult.start()))
        .put("annotationEnd", String.valueOf(matchResult.end()));
  }
}
