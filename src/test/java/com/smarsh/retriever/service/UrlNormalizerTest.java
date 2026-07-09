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
  void urlIsRequired() {
    assertThrows(NullPointerException.class, () -> normalizer.normalize(null));
  }
}
