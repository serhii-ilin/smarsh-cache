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
  private final UrlNormalizer urlNormalizer;

  public CachedWebContentRetriever() {
    this(new HttpContentFetcher(), new FileBasedWebContentCache(), new UrlNormalizer());
  }

  CachedWebContentRetriever(
      HttpContentFetcher httpContentFetcher,
      ContentCache contentCache,
      UrlNormalizer urlNormalizer) {
    this.httpContentFetcher = httpContentFetcher;
    this.contentCache = contentCache;
    this.urlNormalizer = urlNormalizer;
  }

  @Override
  public Response retrieve(Request request) {
    Objects.requireNonNull(request, "request is required");
    String url = urlNormalizer.normalize(request.url());
    if (logger.isLoggable(Level.INFO)) {
      logger.info("Retrieving content for URL: " + url);
    }
    Optional<CachedEntry> cached = contentCache.get(url);
    if (cached.isPresent()) {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Returning cached content for URL: " + url);
      }
      CachedEntry entry = cached.get();
      return new Response(url, entry.content(), entry.cachedAt());
    }
    byte[] content = httpContentFetcher.fetch(url);
    logger.log(
        Level.FINE, "Fetched content for URL: " + url + ", size: " + content.length + " bytes");
    OffsetDateTime fetchedAt = OffsetDateTime.now();
    contentCache.put(url, new CachedEntry(content, fetchedAt));
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Cached content for URL: " + url + ", size: " + content.length + " bytes");
    }
    return new Response(url, content, fetchedAt);
  }
}
