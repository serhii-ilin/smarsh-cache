package com.smarsh.retriever.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Pattern;

public class UrlNormalizer {
  private static final String DEFAULT_SCHEME = "https://";
  private static final Pattern SCHEME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");

  public String normalize(String url) {
    Objects.requireNonNull(url, "url is required");
    String trimmed = url.trim();
    String withScheme = SCHEME_PATTERN.matcher(trimmed).find() ? trimmed : DEFAULT_SCHEME + trimmed;
    try {
      return new URI(withScheme).normalize().toString();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URL: " + url, e);
    }
  }
}
