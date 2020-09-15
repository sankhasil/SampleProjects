/** */
package org.ai4bd.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** @author SankyS */
public class ExtractionMessage implements Cloneable {

  private UUID id;
  private String socketConnectionID;
  private String status;
  private String responseType;
  private Map<String, byte[]> extractedByteMap;
  private byte[] extractedByteArray;
  private Object metaInfo;
  private Map<String, String> failureReasonMap;

  public ExtractionMessage() {}

  @Override
  public ExtractionMessage clone() throws CloneNotSupportedException {
    return (ExtractionMessage) super.clone();
  }

  public ExtractionMessage(UUID id, String status) {
    this.id = id;
    this.status = status;
  }

  public String getSocketConnectionID() {
    return socketConnectionID;
  }

  public void setSocketConnectionID(String socketConnectionID) {
    this.socketConnectionID = socketConnectionID;
  }

  public Object getMetaInfo() {
    return metaInfo;
  }

  public void setMetaInfo(Object metaInfo) {
    this.metaInfo = metaInfo;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  public Map<String, String> getFailureReasonMap() {
    if (this.failureReasonMap == null) {
      this.failureReasonMap = new HashMap<>();
    }
    return this.failureReasonMap;
  }

  public void addFailureReason(String reasonFor, String reasonContent) {
    if (this.failureReasonMap == null) {
      this.failureReasonMap = new HashMap<>();
    }
    this.failureReasonMap.putIfAbsent(reasonFor, reasonContent);
  }

  public Map<String, byte[]> getExtractedByteMap() {
    return extractedByteMap;
  }

  public void addExtractedByte(String name, byte[] content) {
    if (this.extractedByteMap == null) {
      this.extractedByteMap = new HashMap<>();
    }
    this.extractedByteMap.putIfAbsent(name, content);
  }

  public byte[] getExtractedByteArray() {
    if (extractedByteArray != null) {
      return extractedByteArray.clone();
    }
    return null;
  }

  public void setExtractedByteArray(byte[] extractedByteArray) {
    this.extractedByteArray = extractedByteArray.clone();
  }
}
