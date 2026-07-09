package com.smarsh.retriever.service;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

public record CachedEntry(byte[] content, OffsetDateTime cachedAt) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
