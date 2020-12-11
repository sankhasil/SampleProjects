//package org.ai4bd.test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.apache.commons.lang3.StringUtils;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.DisabledOnOs;
//import org.junit.jupiter.api.condition.OS;
//import org.playground.util.CommonUtils;
//
//@DisplayName("Common Util Tests")
//public class CommonUtilTest {
//  
//  @BeforeAll
//  static void init() {
//    CommonUtils.setEnv("TEST_ENV_NUMBER", "2000");
//    CommonUtils.setEnv("TEST_ENV_STRING", "Example");
//  }
//
//  @AfterAll
//  static void cleanup() {
//    CommonUtils.setEnv("TEST_ENV_NUMBER", "");
//    CommonUtils.setEnv("TEST_ENV_STRING", "");
//  }
//
//  @Test
//  @DisabledOnOs({OS.WINDOWS})
//  void testGetEnvAsLong() {
//    assertEquals(2000l, CommonUtils.getEnvValueAsLong("TEST_ENV_NUMBER", "23"),"The environment value must be equal.");
//  }
//  
//  @Test
//  @DisabledOnOs({OS.WINDOWS})
//  void testGetEnvAsString() {
//    assertEquals("Example", CommonUtils.getEnvValue("TEST_ENV_STRING"),"The environment value must be equal.");
//  }
//  @Test
//  @DisabledOnOs({OS.WINDOWS})
//  void testGetEnvNotPresent() {
//    assertEquals(StringUtils.EMPTY, CommonUtils.getEnvValue("Not present"),"The environment value must be empty.");
//  }
//  @Test
//  @DisabledOnOs({OS.WINDOWS})
//  void testGetEnvDefault() {
//    assertEquals("default", CommonUtils.getEnvValue("Not present","default"),"The environment value must be \"default\".");
//  }
//  
//  
//}
