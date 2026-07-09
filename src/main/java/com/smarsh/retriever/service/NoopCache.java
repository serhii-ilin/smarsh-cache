package com.smarsh.retriever.service;

import java.util.Optional;

/**
 * A {@link ContentCache} that never stores anything. Every lookup is a miss, so content is always
 * fetched fresh. Useful for disabling caching (e.g. in tests or ad-hoc runs).
 */
public class NoopCache implements ContentCache {
  @Override
  public Optional<CachedEntry> get(String url) {
    return Optional.empty();
  }

  @Override
  public void put(String url, CachedEntry entry) {
    // Intentionally does nothing.
  }
}
