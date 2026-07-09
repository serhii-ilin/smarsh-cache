package com.smarsh.retriever.service;

public record Request(String url) {
  public static Request of(String url) {
    return new Request(url);
  }
}
