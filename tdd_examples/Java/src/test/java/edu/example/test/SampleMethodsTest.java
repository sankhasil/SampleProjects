/**
 * 
 */
package edu.example.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import edu.example.samples.SampleMethods;

/**
 * @author SankyS
 *
 */

public class SampleMethodsTest {

  public static SampleMethods sampleMethods;

  @BeforeAll
  public static void init() {
    sampleMethods = new SampleMethods();
  }

  @DisplayName("Happy Path Test Add Two Numbers from Sample Methods")
  @Test
  public void testAddTwoNumbersHappyPath() {
    assertEquals(sampleMethods.addTwoNumbers(12, 12), 24, "Result should be 24");
  }
  
  @DisplayName("Negative Test Add Two Numbers from Sample Methods")
  @Test
  public void testAddTwoNumbersNegative() {
    assertEquals(sampleMethods.addTwoNumbers(-12, 13), -1, "Result should be -1");
  }

  @Test
  public void addEmptyInput() {
    assertEquals(sampleMethods.add(""), 0, "Empty input should return 0");
  }

  @Test
  public void addOneNumberInput() {
    assertEquals(sampleMethods.add("1"), 1, "Result should be 1");
  }

  @Test
  public void addTwoNumberInput() {
    assertEquals(sampleMethods.add("1,2"), 3, "Result should be 3");
  }
}
