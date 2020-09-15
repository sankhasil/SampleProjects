/**
 * 
 */
package org.playground.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.playground.util.CommonUtils;

/**
 * @author SankyS
 *
 */

public class CommonUtilsTest {

  @Test
  public void testConvertLanguageCodeToThreeLetterCode() {
    assertEquals("Method must return three lettered language code", "eng+fra+deu",
        CommonUtils.convertLanguageCodeToThreeLetterCode("eng+fr+deu"));
  }

  @Test
  public void testCheckIfLanguageCodesExistsForThreeLetters() {
    assertTrue(CommonUtils.checkIfLanguageCodesExists("eng+fra"));
  }


  @Test
  public void testCheckIfLanguageCodesExistsForTwoLetters() {
    assertTrue(CommonUtils.checkIfLanguageCodesExists("de+it"));
  }

  @Test
  public void testCheckIfLanguageCodesExistsForMixedLetters() {
    assertTrue(CommonUtils.checkIfLanguageCodesExists("eng+fr+It+zu"));
  }
  @Test
  public void testTailingColonRemoval() {
    assertTrue(CommonUtils.tailingColonRemoval("asf:\n\t").equalsIgnoreCase("asf"));
  }
}
