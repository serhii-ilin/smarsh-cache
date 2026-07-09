package com.smarsh.retriever.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UrlNormalizerTest {

  private final UrlNormalizer normalizer = new UrlNormalizer();

  @Test
  void addsDefaultSchemeWhenMissing() {
    assertEquals("https://example.com/path", normalizer.normalize("example.com/path"));
  }

  @Test
  void keepsExistingScheme() {
    assertEquals("http://example.com/path", normalizer.normalize("http://example.com/path"));
  }

  @Test
  void collapsesDotSegments() {
    assertEquals("http://example.com/b", normalizer.normalize("http://example.com/a/../b"));
  }

  @Test
  void trimsWhitespace() {
    assertEquals("https://example.com/path", normalizer.normalize("  example.com/path  "));
  }

  @Test
  void lowercasesScheme() {
    assertEquals("http://example.com/path", normalizer.normalize("HTTP://example.com/path"));
  }

  @Test
  void lowercasesHost() {
    assertEquals("http://example.com/path", normalizer.normalize("http://EXAMPLE.COM/path"));
  }

  @Test
  void lowercasesSchemeAndHostTogether() {
    assertEquals("https://example.com/", normalizer.normalize("HTTPS://Example.Com/"));
  }

  @Test
  void preservesPathCase() {
    assertEquals(
        "http://example.com/Path/To/Resource",
        normalizer.normalize("http://EXAMPLE.com/Path/To/Resource"));
  }

  @Test
  void preservesQueryAndFragmentCase() {
    assertEquals(
        "http://example.com/p?Key=Value#Section",
        normalizer.normalize("http://Example.com/p?Key=Value#Section"));
  }

  @Test
  void preservesPortWhileLowercasingHost() {
    assertEquals(
        "http://example.com:8080/path", normalizer.normalize("http://EXAMPLE.com:8080/path"));
  }

  @Test
  void urlIsRequired() {
    assertThrows(NullPointerException.class, () -> normalizer.normalize(null));
  }
}
