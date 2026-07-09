package com.smarsh.retriever.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileBasedWebContentCache implements ContentCache {
  private static final Logger logger =
      Logger.getLogger(FileBasedWebContentCache.class.getCanonicalName());
  private static final Path DEFAULT_DIRECTORY = Path.of(".cache");

  private final Path directory;

  public FileBasedWebContentCache() {
    this(DEFAULT_DIRECTORY);
  }

  public FileBasedWebContentCache(Path directory) {
    this.directory = directory;
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create cache directory: " + directory, e);
    }
  }

  @Override
  public Optional<CachedEntry> get(String url) {
    Path file = fileFor(url);
    logger.log(Level.FINE, "Looking for cache file: " + file);
    if (!Files.isRegularFile(file)) {
      return Optional.empty();
    }
    try {
      return Optional.of(deserialize(Files.readAllBytes(file)));
    } catch (IOException | ClassNotFoundException | RuntimeException e) {
      logger.log(Level.FINE, "Failed to read cache file: " + file, e);
      return Optional.empty();
    }
  }

  @Override
  public void put(String url, CachedEntry entry) {
    Path file = fileFor(url);
    try {
      Files.write(file, serialize(entry));
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to write cache file: " + file, e);
    }
  }

  private Path fileFor(String url) {
    return directory.resolve(hash(url));
  }

  private static String hash(String url) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(url.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      logger.log(Level.WARNING, "Failed to hash cache file: " + url, e);
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }

  private static byte[] serialize(CachedEntry entry) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      objectStream.writeObject(entry);
    }
    return byteStream.toByteArray();
  }

  private static CachedEntry deserialize(byte[] data) throws IOException, ClassNotFoundException {
    try (ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
      return (CachedEntry) objectStream.readObject();
    }
  }
}
