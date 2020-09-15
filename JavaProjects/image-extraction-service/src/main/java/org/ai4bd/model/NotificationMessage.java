package org.ai4bd.model;

public class NotificationMessage {

  private String jobId;
  private String documentName;
  private String pageFileName;
  private String documentPath;
  private int pageCount;
  private int pageIndex;
  private int pageWidth;
  private int pageHeight;
  private String pageContent;
  private int documentCount;
  private int documentIndex;
  private Object meta;

  public NotificationMessage() {}

  public String getJobId() {
    return jobId;
  }

  public String getDocumentName() {
    return documentName;
  }

  public String getPageFileName() {
    return pageFileName;
  }

  public int getPageCount() {
    return pageCount;
  }

  public int getPageIndex() {
    return pageIndex;
  }

  public int getPageWidth() {
    return pageWidth;
  }

  public int getPageHeight() {
    return pageHeight;
  }

  public String getPageContent() {
    return pageContent;
  }

  public int getDocumentCount() {
    return documentCount;
  }

  public int getDocumentIndex() {
    return documentIndex;
  }

  public Object getMeta() {
    return meta;
  }

  public String getDocumentPath() {
    return documentPath;
  }

  public NotificationMessage jobId(String jobId) {
    this.jobId = jobId;
    return this;
  }

  public NotificationMessage documentName(String documentName) {
    this.documentName = documentName;
    return this;
  }

  public NotificationMessage pageFileName(String pageFileName) {
    this.pageFileName = pageFileName;
    return this;
  }

  public NotificationMessage pageCount(int pageCount) {
    this.pageCount = pageCount;
    return this;
  }

  public NotificationMessage pageIndex(int pageIndex) {
    this.pageIndex = pageIndex;
    return this;
  }

  public NotificationMessage pageWidth(int pageWidth) {
    this.pageWidth = pageWidth;
    return this;
  }

  public NotificationMessage pageHeight(int pageHeight) {
    this.pageHeight = pageHeight;
    return this;
  }

  public NotificationMessage pageContent(String pageContent) {
    this.pageContent = pageContent;
    return this;
  }

  public NotificationMessage documentCount(int documentCount) {
    this.documentCount = documentCount;
    return this;
  }

  public NotificationMessage documentIndex(int documentIndex) {
    this.documentIndex = documentIndex;
    return this;
  }

  public NotificationMessage meta(Object meta) {
    this.meta = meta;
    return this;
  }

  public NotificationMessage documentPath(String documentPath) {
    this.documentPath = documentPath;
    return this;
  }
}
