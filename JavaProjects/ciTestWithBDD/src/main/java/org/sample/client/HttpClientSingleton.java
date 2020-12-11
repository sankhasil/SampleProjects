/**
 * 
 */
package org.sample.client;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import org.sample.util.CommonUtils;

/**
 * @author SankyS
 *
 */
public final class HttpClientSingleton {

  private static HttpClientSingleton INSTANCE;
  private HttpClient client;
  private Builder requestBuilder;
  
  private HttpClientSingleton() {
    long connectionTimeOut = CommonUtils.getEnvValueAsLong("CONNECTION_TIMEOUT", "50");
    client = HttpClient.newBuilder().version(Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(connectionTimeOut)).build();
  }

  public static HttpClientSingleton getInstance() {
    if (INSTANCE == null) {
      synchronized (HttpClientSingleton.class) {
        if (INSTANCE == null) {
          INSTANCE = new HttpClientSingleton();
        }
      }
    }
    return INSTANCE;
  }

  public HttpClient getClient() {
    return client;
  }

  public Builder getRequestBuilder() {
    return HttpRequest.newBuilder().setHeader("User-Agent", "Java based Jenkin CI Test");
  }
  
}
