package org.sample.execution;

import org.sample.client.HttpClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author SankyS
 */
public class HttpRequester {

  private static final Logger logger = LoggerFactory.getLogger(HttpRequester.class);

  private static final Path dataPath =
      FileSystems.getDefault().getPath("").toAbsolutePath().getParent().resolve("data");

  public HttpResponse<String> postWithFile(String endpointUrl, String fileName,
      Map<String, String> headerMap) {
    try {
      byte[] byteData = Files.readAllBytes(dataPath.resolve(fileName));
      HttpRequest postRequest =
          addHeaderParamToRequest(HttpClientSingleton.getInstance().getRequestBuilder(), headerMap)
              .uri(URI.create(endpointUrl)).POST(BodyPublishers.ofByteArray(byteData)).build();

      logger.info("Sending POST request with file \"{}\" to \"{}\"", fileName, endpointUrl);
      HttpResponse<String> response =
          HttpClientSingleton.getInstance().getClient().send(postRequest, BodyHandlers.ofString());
      logger.info("Received response for request with file \"{}\" with code <{}>", fileName,
          response.statusCode());

      return response;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  public HttpResponse<String> postWithParam(String endpointUrl, Map<Object, Object> paramMap) {
    try {
      HttpRequest postRequest = HttpClientSingleton.getInstance().getRequestBuilder()
          .POST(ofFormData(paramMap)).uri(URI.create(endpointUrl))
          .header("Content-Type", "application/x-www-form-urlencoded").build();

      logger.info("Sending POST request with params to \"{}\"", endpointUrl);
      HttpResponse<String> response =
          HttpClientSingleton.getInstance().getClient().send(postRequest, BodyHandlers.ofString());
      logger.info("Received response for request with param with code <{}>", response.statusCode());

      return response;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public HttpResponse<String> getRequest(String endpointUrl, Map<String, String> headerMap) {
    try {
      HttpRequest getRequest =
          addHeaderParamToRequest(HttpClientSingleton.getInstance().getRequestBuilder(), headerMap)
              .uri(URI.create(endpointUrl)).GET().build();

      logger.info("Sending POST request with params to \"{}\"", endpointUrl);
      HttpResponse<String> response =
          HttpClientSingleton.getInstance().getClient().send(getRequest, BodyHandlers.ofString());
      logger.info("Received response with code <{}>", response.statusCode());

      return response;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;


  }

  private Builder addHeaderParamToRequest(Builder requestBuilder, Map<String, String> headerMap) {
    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
      requestBuilder.setHeader(entry.getKey(), entry.getValue());
    }
    return requestBuilder;
  }

  private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
    var builder = new StringBuilder();
    if (data != null && data.size() > 0) {
      for (Map.Entry<Object, Object> entry : data.entrySet()) {
        if (builder.length() > 0) {
          builder.append("&");
        }
        builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
        builder.append("=");
        builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
      }
    }
    return HttpRequest.BodyPublishers.ofString(builder.toString());
  }
}
