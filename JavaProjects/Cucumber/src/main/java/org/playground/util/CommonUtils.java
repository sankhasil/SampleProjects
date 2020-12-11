package org.playground.util;

import java.lang.reflect.Field;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public final class CommonUtils {

  public static String getEnvValue(String key) {
    String value = System.getenv(key);
    if(StringUtils.isNotBlank(value)) {
      return value;
    }
    return "";
  }
  
  public static String getEnvValue(String key,String defaultValue) {
    String value = getEnvValue(key);
    if(StringUtils.isBlank(value)) {
      value = defaultValue;
    }
    return value;
  } 
  
  public static long getEnvValueAsLong(String key,String defaultValue) {
    String value = getEnvValue(key);
    if(StringUtils.isBlank(value)) {
      value = defaultValue;
    }
    try {
      return Long.parseLong(value);
    }catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  } 
  
  public static void setEnv(String key, String value) {
    try {
        Map<String, String> env = System.getenv();
        Class<?> cl = env.getClass();
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        Map<String, String> writableEnv = (Map<String, String>) field.get(env);
        writableEnv.put(key, value);
    } catch (Exception e) {
        throw new IllegalStateException("Failed to set environment variable", e);
    }
}
}
