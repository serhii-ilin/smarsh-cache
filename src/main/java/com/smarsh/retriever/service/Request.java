package com.smarsh.retriever.service;

public class Request {
  private final String url;

  private Request(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public static Request of(String url) {
    return new Request(url);
  }
}
