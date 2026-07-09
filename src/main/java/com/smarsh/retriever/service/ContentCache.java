package com.smarsh.retriever.service;

import java.util.Optional;

public interface ContentCache {
  Optional<CachedEntry> get(String url);

  void put(String url, CachedEntry entry);
}
