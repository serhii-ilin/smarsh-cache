package com.smarsh.retriever.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileBasedWebContentCacheTest {

  @TempDir Path tempDir;

  private final List<LogRecord> logRecords = new java.util.ArrayList<>();
  private Handler captureHandler;
  private Logger logger;

  @BeforeEach
  void captureLogs() {
    logger = Logger.getLogger(FileBasedWebContentCache.class.getCanonicalName());
    captureHandler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            logRecords.add(record);
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    logger.addHandler(captureHandler);
  }

  @AfterEach
  void releaseLogs() {
    logger.removeHandler(captureHandler);
  }

  @Test
  void returnsEmptyWhenNothingCached() {
    FileBasedWebContentCache cache = new FileBasedWebContentCache(tempDir);

    assertTrue(cache.get("http://example.invalid/content").isEmpty());
  }

  @Test
  void putThenGetReturnsStoredEntry() {
    FileBasedWebContentCache cache = new FileBasedWebContentCache(tempDir);
    String url = "http://example.invalid/content";
    byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
    OffsetDateTime cachedAt = OffsetDateTime.parse("2026-01-01T12:34:56.789Z");

    cache.put(url, new CachedEntry(content, cachedAt));
    Optional<CachedEntry> result = cache.get(url);

    assertTrue(result.isPresent());
    assertEquals("hello world", new String(result.get().content(), StandardCharsets.UTF_8));
    assertEquals(cachedAt, result.get().cachedAt());
  }

  @Test
  void usesFlatFileNameHashedFromUrl() throws NoSuchAlgorithmException, IOException {
    FileBasedWebContentCache cache = new FileBasedWebContentCache(tempDir);
    String url = "http://example.invalid/content";

    cache.put(url, new CachedEntry("data".getBytes(StandardCharsets.UTF_8), OffsetDateTime.now()));

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    String expectedFileName =
        HexFormat.of().formatHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
    Path expectedFile = tempDir.resolve(expectedFileName);

    assertTrue(Files.isRegularFile(expectedFile));
    // The serialized entry plus its ".content" debug dump.
    try (var entries = Files.list(tempDir)) {
      assertEquals(2, entries.count());
    }
  }

  @Test
  void alsoDumpsRawContentWithContentExtension() throws NoSuchAlgorithmException, IOException {
    FileBasedWebContentCache cache = new FileBasedWebContentCache(tempDir);
    String url = "http://example.invalid/content";
    byte[] content = "raw page source".getBytes(StandardCharsets.UTF_8);

    cache.put(url, new CachedEntry(content, OffsetDateTime.now()));

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    String hash = HexFormat.of().formatHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
    Path contentFile = tempDir.resolve(hash + ".content");

    assertTrue(Files.isRegularFile(contentFile));
    assertArrayEquals(content, Files.readAllBytes(contentFile));
  }

  @Test
  void returnsEmptyAndLogsWarningWhenFileIsMalformed() throws IOException {
    FileBasedWebContentCache cache = new FileBasedWebContentCache(tempDir);
    String url = "http://example.invalid/content";
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    String fileName = HexFormat.of().formatHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
    Files.write(
        tempDir.resolve(fileName), "not a valid cache entry".getBytes(StandardCharsets.UTF_8));

    Optional<CachedEntry> result = cache.get(url);

    assertTrue(result.isEmpty());
    assertFalse(logRecords.isEmpty());
    assertTrue(logRecords.stream().anyMatch(r -> r.getLevel() == Level.WARNING));
  }
}
