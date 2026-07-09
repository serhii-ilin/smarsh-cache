package com.smarsh.retriever.service;

import java.io.Serializable;
import java.time.OffsetDateTime;

public record CachedEntry(byte[] content, OffsetDateTime cachedAt) implements Serializable {}
