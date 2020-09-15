package org.ai4bd.model;

public class RequestContent {

  private final String contentType;
  private final byte[] content;

  public RequestContent(String contentType, byte[] content) {
    this.contentType = contentType;
    this.content = content.clone();
  }

  public String getContentType() {
    return contentType;
  }

  public byte[] getContent() {
    return content.clone();
  }
}
