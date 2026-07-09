package com.smarsh.retriever.service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CachedWebContentRetriever implements ContentRetriever {
  private static final Logger logger =
      Logger.getLogger(CachedWebContentRetriever.class.getCanonicalName());

  private final HttpContentFetcher httpContentFetcher;
  private final ContentCache contentCache;

  public CachedWebContentRetriever() {
    this(new HttpContentFetcher(), new FileBasedWebContentCache());
  }

  CachedWebContentRetriever(HttpContentFetcher httpContentFetcher, ContentCache contentCache) {
    this.httpContentFetcher = httpContentFetcher;
    this.contentCache = contentCache;
  }

  @Override
  public Response retrieve(Request request) {
    Objects.requireNonNull(request, "request is required");
    if (logger.isLoggable(Level.INFO)) {
      logger.info("Retrieving content for URL: " + request.getUrl());
    }
    Optional<CachedEntry> cached = contentCache.get(request.getUrl());
    if (cached.isPresent()) {
      CachedEntry entry = cached.get();
      return new Response(request.getUrl(), entry.content(), entry.cachedAt());
    }
    byte[] content = httpContentFetcher.fetch(request.getUrl());
    logger.log(
        Level.FINE,
        "Fetched content for URL: " + request.getUrl() + ", size: " + content.length + " bytes");
    OffsetDateTime fetchedAt = OffsetDateTime.now();
    contentCache.put(request.getUrl(), new CachedEntry(content, fetchedAt));
    return new Response(request.getUrl(), content, fetchedAt);
  }
}
