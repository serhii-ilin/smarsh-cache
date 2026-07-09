package com.smarsh.retriever.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedWebContentRetrieverTest {

  @Mock private HttpContentFetcher httpContentFetcher;

  @Mock private ContentCache contentCache;

  @Test
  void requestIsRequired() {
    CachedWebContentRetriever retriever =
        new CachedWebContentRetriever(httpContentFetcher, contentCache);

    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> retriever.retrieve(null));

    assertEquals("request is required", exception.getMessage());
  }

  @Test
  void fetchesContentOnCacheMiss() {
    String url = "http://example.invalid/content";
    byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
    when(contentCache.get(url)).thenReturn(Optional.empty());
    when(httpContentFetcher.fetch(url)).thenReturn(body);
    CachedWebContentRetriever retriever =
        new CachedWebContentRetriever(httpContentFetcher, contentCache);

    Response response = retriever.retrieve(Request.of(url));

    assertEquals(url, response.url());
    assertEquals("hello world", new String(response.content(), StandardCharsets.UTF_8));
    assertNotNull(response.createdAt());
  }

  @Test
  void returnsCachedContentWithoutFetchingOnCacheHit() {
    String url = "http://example.invalid/content";
    byte[] body = "from cache".getBytes(StandardCharsets.UTF_8);
    OffsetDateTime cachedAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
    when(contentCache.get(url)).thenReturn(Optional.of(new CachedEntry(body, cachedAt)));
    CachedWebContentRetriever retriever =
        new CachedWebContentRetriever(httpContentFetcher, contentCache);

    Response response = retriever.retrieve(Request.of(url));

    assertEquals(url, response.url());
    assertEquals("from cache", new String(response.content(), StandardCharsets.UTF_8));
    assertEquals(cachedAt, response.createdAt());
    verify(httpContentFetcher, never()).fetch(anyString());
  }

  @Test
  void storesFetchedContentInCacheOnCacheMiss() {
    String url = "http://example.invalid/content";
    byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
    when(contentCache.get(url)).thenReturn(Optional.empty());
    when(httpContentFetcher.fetch(url)).thenReturn(body);
    CachedWebContentRetriever retriever =
        new CachedWebContentRetriever(httpContentFetcher, contentCache);

    Response response = retriever.retrieve(Request.of(url));

    ArgumentCaptor<CachedEntry> entryCaptor = ArgumentCaptor.forClass(CachedEntry.class);
    verify(contentCache).put(eq(url), entryCaptor.capture());
    CachedEntry storedEntry = entryCaptor.getValue();
    assertEquals("hello world", new String(storedEntry.content(), StandardCharsets.UTF_8));
    assertEquals(response.createdAt(), storedEntry.cachedAt());
  }
}
