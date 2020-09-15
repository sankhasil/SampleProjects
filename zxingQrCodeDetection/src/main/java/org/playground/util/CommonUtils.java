/**
 * 
 */
package org.playground.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * @author SankyS
 *
 */
public final class CommonUtils {

  private final static List<String> languageCodeList =
      Collections.unmodifiableList(Arrays.asList("abk", "aar", "afr", "aka", "sqi", "amh", "ara",
          "arg", "hye", "asm", "ava", "ave", "aym", "aze", "bam", "bak", "eus", "bel", "ben", "bih",
          "bis", "bos", "bre", "bul", "mya", "cat", "cha", "che", "nya", "zho", "chv", "cor", "cos",
          "cre", "hrv", "ces", "dan", "div", "nld", "dzo", "eng", "epo", "est", "ewe", "fao", "fij",
          "fin", "fra", "ful", "glg", "kat", "deu", "ell", "grn", "guj", "hat", "hau", "heb", "her",
          "hin", "hmo", "hun", "ina", "ind", "ile", "gle", "ibo", "ipk", "ido", "isl", "ita", "iku",
          "jpn", "jav", "kal", "kan", "kau", "kas", "kaz", "khm", "kik", "kin", "kir", "kom", "kon",
          "kor", "kur", "kua", "lat", "ltz", "lug", "lim", "lin", "lao", "lit", "lub", "lav", "glv",
          "mkd", "mlg", "msa", "mal", "mlt", "mri", "mar", "mah", "mon", "nau", "nav", "nde", "nep",
          "ndo", "nob", "nno", "nor", "iii", "nbl", "oci", "oji", "chu", "orm", "ori", "oss", "pan",
          "pli", "fas", "pol", "pus", "por", "que", "roh", "run", "ron", "rus", "san", "srd", "snd",
          "sme", "smo", "sag", "srp", "gla", "sna", "sin", "slk", "slv", "som", "sot", "spa", "sun",
          "swa", "ssw", "swe", "tam", "tel", "tgk", "tha", "tir", "bod", "tuk", "tgl", "tsn", "ton",
          "tur", "tso", "tat", "twi", "tah", "uig", "ukr", "urd", "uzb", "ven", "vie", "vol", "wln",
          "cym", "wol", "fry", "xho", "yid", "yor", "zha", "zul"));

  private final static JSONObject languageJson = new JSONObject(
      "{\"ab\":\"abk\",\"aa\":\"aar\",\"af\":\"afr\",\"ak\":\"aka\",\"sq\":\"sqi\",\"am\":\"amh\",\"ar\":\"ara\",\"an\":\"arg\",\"hy\":\"hye\",\"as\":\"asm\",\"av\":\"ava\",\"ae\":\"ave\",\"ay\":\"aym\",\"az\":\"aze\",\"bm\":\"bam\",\"ba\":\"bak\",\"eu\":\"eus\",\"be\":\"bel\",\"bn\":\"ben\",\"bh\":\"bih\",\"bi\":\"bis\",\"bs\":\"bos\",\"br\":\"bre\",\"bg\":\"bul\",\"my\":\"mya\",\"ca\":\"cat\",\"ch\":\"cha\",\"ce\":\"che\",\"ny\":\"nya\",\"zh\":\"zho\",\"cv\":\"chv\",\"kw\":\"cor\",\"co\":\"cos\",\"cr\":\"cre\",\"hr\":\"hrv\",\"cs\":\"ces\",\"da\":\"dan\",\"dv\":\"div\",\"nl\":\"nld\",\"dz\":\"dzo\",\"en\":\"eng\",\"eo\":\"epo\",\"et\":\"est\",\"ee\":\"ewe\",\"fo\":\"fao\",\"fj\":\"fij\",\"fi\":\"fin\",\"fr\":\"fra\",\"ff\":\"ful\",\"gl\":\"glg\",\"ka\":\"kat\",\"de\":\"deu\",\"el\":\"ell\",\"gn\":\"grn\",\"gu\":\"guj\",\"ht\":\"hat\",\"ha\":\"hau\",\"he\":\"heb\",\"hz\":\"her\",\"hi\":\"hin\",\"ho\":\"hmo\",\"hu\":\"hun\",\"ia\":\"ina\",\"id\":\"ind\",\"ie\":\"ile\",\"ga\":\"gle\",\"ig\":\"ibo\",\"ik\":\"ipk\",\"io\":\"ido\",\"is\":\"isl\",\"it\":\"ita\",\"iu\":\"iku\",\"ja\":\"jpn\",\"jv\":\"jav\",\"kl\":\"kal\",\"kn\":\"kan\",\"kr\":\"kau\",\"ks\":\"kas\",\"kk\":\"kaz\",\"km\":\"khm\",\"ki\":\"kik\",\"rw\":\"kin\",\"ky\":\"kir\",\"kv\":\"kom\",\"kg\":\"kon\",\"ko\":\"kor\",\"ku\":\"kur\",\"kj\":\"kua\",\"la\":\"lat\",\"lb\":\"ltz\",\"lg\":\"lug\",\"li\":\"lim\",\"ln\":\"lin\",\"lo\":\"lao\",\"lt\":\"lit\",\"lu\":\"lub\",\"lv\":\"lav\",\"gv\":\"glv\",\"mk\":\"mkd\",\"mg\":\"mlg\",\"ms\":\"msa\",\"ml\":\"mal\",\"mt\":\"mlt\",\"mi\":\"mri\",\"mr\":\"mar\",\"mh\":\"mah\",\"mn\":\"mon\",\"na\":\"nau\",\"nv\":\"nav\",\"nd\":\"nde\",\"ne\":\"nep\",\"ng\":\"ndo\",\"nb\":\"nob\",\"nn\":\"nno\",\"no\":\"nor\",\"ii\":\"iii\",\"nr\":\"nbl\",\"oc\":\"oci\",\"oj\":\"oji\",\"cu\":\"chu\",\"om\":\"orm\",\"or\":\"ori\",\"os\":\"oss\",\"pa\":\"pan\",\"pi\":\"pli\",\"fa\":\"fas\",\"pl\":\"pol\",\"ps\":\"pus\",\"pt\":\"por\",\"qu\":\"que\",\"rm\":\"roh\",\"rn\":\"run\",\"ro\":\"ron\",\"ru\":\"rus\",\"sa\":\"san\",\"sc\":\"srd\",\"sd\":\"snd\",\"se\":\"sme\",\"sm\":\"smo\",\"sg\":\"sag\",\"sr\":\"srp\",\"gd\":\"gla\",\"sn\":\"sna\",\"si\":\"sin\",\"sk\":\"slk\",\"sl\":\"slv\",\"so\":\"som\",\"st\":\"sot\",\"es\":\"spa\",\"su\":\"sun\",\"sw\":\"swa\",\"ss\":\"ssw\",\"sv\":\"swe\",\"ta\":\"tam\",\"te\":\"tel\",\"tg\":\"tgk\",\"th\":\"tha\",\"ti\":\"tir\",\"bo\":\"bod\",\"tk\":\"tuk\",\"tl\":\"tgl\",\"tn\":\"tsn\",\"to\":\"ton\",\"tr\":\"tur\",\"ts\":\"tso\",\"tt\":\"tat\",\"tw\":\"twi\",\"ty\":\"tah\",\"ug\":\"uig\",\"uk\":\"ukr\",\"ur\":\"urd\",\"uz\":\"uzb\",\"ve\":\"ven\",\"vi\":\"vie\",\"vo\":\"vol\",\"wa\":\"wln\",\"cy\":\"cym\",\"wo\":\"wol\",\"fy\":\"fry\",\"xh\":\"xho\",\"yi\":\"yid\",\"yo\":\"yor\",\"za\":\"zha\",\"zu\":\"zul\"}");

  /**
   * Check for valid language code.
   * 
   * @param languageCode
   * @return true or false {@link Boolean}
   */
  public static boolean checkIfLanguageCodesExists(String languageCode) {
    return languageCodeList.containsAll(Arrays.asList(languageCode.split("\\+")).parallelStream()
        .map(code -> getThreeLetterLanguageCode(code)).collect(Collectors.toList()));
  }

  /**
   * Check for valid page segmentation mode.
   * <table id="memberList">
   * <tbody>
   * <tr>
   * <th>Member name</th>
   * <th>Value</th>
   * <th>Description</th>
   * <tr>
   * <td>RIL_BLOCK></td>
   * <td>0</td>
   * <td>Block of text/image/separator line.</td>
   * </tr>
   * <tr>
   * <td>RIL_PARA</td>
   * <td>1</td>
   * <td>Paragraph within a block.</td>
   * </tr>
   * <tr>
   * <td>RIL_TEXTLINE</td>
   * <td>2</td>
   * <td>Line within a paragraph.</td>
   * </tr>
   * <tr>
   * <td>RIL_WORD</td>
   * <td>3</td>
   * <td>Word within a textline.</td>
   * </tr>
   * <tr>
   * <td>RIL_SYMBOL</td>
   * <td>4</td>
   * <td>Symbol/character within a word.</td>
   * </tr>
   * </tbody>
   * </table>
   * 
   * @param psm {@link Integer}
   * @return true or false {@link Boolean}
   */
  public static boolean checkIfPageSegmentationModeExists(int psm) {
    return psm >= 0 && psm <= 13;
  }



  /**
   * Provides the Three lettered language code if two lettered ISO language code is supplied
   * 
   * @param languageCodeToFind
   * @return languageCode {@link String}
   */
  public static String getThreeLetterLanguageCode(String languageCodeToFind) {
    if (languageCodeToFind.length() == 2) {
      String languageCode = languageJson.getString(languageCodeToFind.toLowerCase());
      if (StringUtils.isNotBlank(languageCode))
        return languageCode;
    }
    return languageCodeToFind.toLowerCase();
  }

  /**
   * Converts two lettered or mixed language codes into three lettered code even if String is
   * separated by '+'
   * 
   * @param languageCode
   * @return languageCode {@link String}
   */
  public static String convertLanguageCodeToThreeLetterCode(String languageCode) {
    if (languageCode.contains("+")) {
      StringJoiner languageCodeJoiner = new StringJoiner("+");
      for (String code : languageCode.split("\\+"))
        languageCodeJoiner.add(getThreeLetterLanguageCode(code));
      return languageCodeJoiner.toString();
    }
    return getThreeLetterLanguageCode(languageCode);
  }

  /**
   * Check for valid page iteration level.
   * <table id="memberList">
   * <tbody>
   * <tr>
   * <th>Member name</th>
   * <th>Value</th>
   * <th>Description</th>
   * </tr>
   * <tr>
   * <td>RIL_BLOCK</td>
   * <td>0</td>
   * <td>Block of text/image/separator line.</td>
   * </tr>
   * <tr>
   * <td>RIL_PARA</td>
   * <td>1</td>
   * <td>Paragraph within a block.</td>
   * </tr>
   * <tr>
   * <td>RIL_TEXTLINE</td>
   * <td>2</td>
   * <td>Line within a paragraph.</td>
   * </tr>
   * <tr>
   * <td>RIL_WORD</td>
   * <td>3</td>
   * <td>Word within a textline.</td>
   * </tr>
   * <tr>
   * <td>RIL_SYMBOL</td>
   * <td>4</td>
   * <td>Symbol/character within a word.</td>
   * </tr>
   * </tbody>
   * </table>
   * 
   * @param pageIterationLevel {@link Integer}
   * @return true or false {@link Boolean}
   */
  public static boolean checkIfPageIterationLevelExists(int pageIterationLevel) {
    return pageIterationLevel >= 0 && pageIterationLevel <= 4;
  }

  /**
   * Checks and removes if necessary the leading colon from the string.
   *
   * @param ocr
   * @return String without leading colon.
   */
  public static String leadingColonRemoval(String ocr) {

    if (StringUtils.isNotBlank(ocr) && ocr.charAt(0) == ':') {
      ocr = ocr.substring(1);
    }

    return ocr;
  }

  /**
   * Checks and removes if necessary the tailing colon from the string. In the case, that the string
   * ends with a colon which is then followed by some white characters like " ", "\n" or "\t", only
   * the semicolon is removed.
   *
   * @param ocr
   * @return String without tailing colon.
   */
  public static String tailingColonRemoval(String ocr) {

    // there is a need to consider cases like "abc:\n\t"
    Matcher columnMatcher = Pattern.compile("(\\:)[\\s]*\\Z", Pattern.DOTALL).matcher(ocr);

    if (columnMatcher.find()) {

      StringBuilder tmpOcr = new StringBuilder(ocr);
      tmpOcr.deleteCharAt(columnMatcher.start(1));

      return tmpOcr.toString().trim();
    }

    return ocr.trim();
  }


}
