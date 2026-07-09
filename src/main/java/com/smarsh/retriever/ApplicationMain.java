package com.smarsh.retriever;

import com.smarsh.retriever.service.CachedWebContentRetriever;
import com.smarsh.retriever.service.ContentRetriever;
import com.smarsh.retriever.service.Request;
import com.smarsh.retriever.service.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ApplicationMain {
  static {
    configureLogger();
  }

  // Just for the simplicity  of the assignment I use JUL logging. Otherwise I would use SLF4J +
  // logback
  private static final Logger logger = Logger.getLogger(ApplicationMain.class.getCanonicalName());

  static void main() {
    logger.info("Starting application...");

    IO.println("Welcome to smarsh retriever application!");

    String url = IO.readln("Enter URL:");
    ContentRetriever retriever = new CachedWebContentRetriever();

    try {
      Response response = retriever.retrieve(Request.of(url));
      IO.println("URL: " + response.url());
      IO.println("Timestamp: " + response.createdAt());
      IO.println("Content: " + new String(response.content(), StandardCharsets.UTF_8));
    } catch (RuntimeException e) {
      logger.log(Level.SEVERE, "Failed to retrieve content for URL: " + url, e);
      IO.println("Failed to retrieve content: " + e.getMessage());
    }

    logger.info("Application finished.");
  }

  // Just for the simplicity  of the assignment I use JUL logging. Otherwise I would use SLF4J +
  // logback
  private static void configureLogger() {
    try (InputStream config = ApplicationMain.class.getResourceAsStream("/logging.properties")) {
      if (config != null) {
        LogManager.getLogManager().readConfiguration(config);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to load logging configuration", e);
    }
  }
}
