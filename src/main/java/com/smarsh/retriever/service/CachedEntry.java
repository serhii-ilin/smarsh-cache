package com.smarsh.retriever.service;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

// Note: "content" is a mutable byte[] exposed directly, so callers could change it.
// For the simplicity of this exercise it is left as-is. In a production app, defensive
// practices would clone the array on the way in (compact constructor) and out (accessor)
// to keep this value object effectively immutable.
public record CachedEntry(byte[] content, OffsetDateTime cachedAt) implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
}
