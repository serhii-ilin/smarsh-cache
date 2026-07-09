package com.smarsh.retriever.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
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
      return lowercaseSchemeAndHost(new URI(withScheme).normalize());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URL: " + url, e);
    }
  }

  // Scheme and host are case-insensitive (RFC 3986), so lowercase them for stable cache keys.
  // Path, query, and fragment are case-sensitive and are left untouched.
  private static String lowercaseSchemeAndHost(URI uri) {
    String host = uri.getHost();
    if (host == null) {
      // Not a server-based authority (unusual for web URLs); leave it as-is.
      return uri.toString();
    }
    StringBuilder result = new StringBuilder();
    result.append(uri.getScheme().toLowerCase(Locale.ROOT)).append("://");
    if (uri.getRawUserInfo() != null) {
      result.append(uri.getRawUserInfo()).append('@');
    }
    result.append(host.toLowerCase(Locale.ROOT));
    if (uri.getPort() != -1) {
      result.append(':').append(uri.getPort());
    }
    if (uri.getRawPath() != null) {
      result.append(uri.getRawPath());
    }
    if (uri.getRawQuery() != null) {
      result.append('?').append(uri.getRawQuery());
    }
    if (uri.getRawFragment() != null) {
      result.append('#').append(uri.getRawFragment());
    }
    return result.toString();
  }
}
