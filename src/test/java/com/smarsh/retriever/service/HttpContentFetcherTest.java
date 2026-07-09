package com.smarsh.retriever.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class HttpContentFetcherTest {

  private HttpServer server;

  @AfterEach
  void stopServer() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void fetchesContentFromUrl() throws IOException {
    byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/content",
        exchange -> {
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();

    String url = "http://localhost:" + server.getAddress().getPort() + "/content";
    HttpContentFetcher fetcher = new HttpContentFetcher(HttpClient.newHttpClient());

    byte[] content = fetcher.fetch(url);

    assertEquals("hello world", new String(content, StandardCharsets.UTF_8));
  }

  @Test
  void followsRedirects() throws IOException {
    byte[] body = "final destination".getBytes(StandardCharsets.UTF_8);
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/target",
        exchange -> {
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.createContext(
        "/source",
        exchange -> {
          exchange.getResponseHeaders().add("Location", "/target");
          exchange.sendResponseHeaders(302, -1);
          exchange.close();
        });
    server.start();

    String url = "http://localhost:" + server.getAddress().getPort() + "/source";
    HttpContentFetcher fetcher = new HttpContentFetcher();

    byte[] content = fetcher.fetch(url);

    assertEquals("final destination", new String(content, StandardCharsets.UTF_8));
  }

  @Test
  void failsFastOnClientErrorWithoutRetrying() throws IOException {
    AtomicInteger requests = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/missing",
        exchange -> {
          requests.incrementAndGet();
          exchange.sendResponseHeaders(404, -1);
          exchange.close();
        });
    server.start();

    String url = "http://localhost:" + server.getAddress().getPort() + "/missing";
    HttpContentFetcher fetcher = new HttpContentFetcher(HttpClient.newHttpClient());

    assertThrows(WebContentRetrievalException.class, () -> fetcher.fetch(url));
    assertEquals(1, requests.get());
  }

  @Test
  void retriesOnServerErrorThenFails() throws IOException {
    AtomicInteger requests = new AtomicInteger();
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/boom",
        exchange -> {
          requests.incrementAndGet();
          exchange.sendResponseHeaders(500, -1);
          exchange.close();
        });
    server.start();

    String url = "http://localhost:" + server.getAddress().getPort() + "/boom";
    HttpContentFetcher fetcher =
        new HttpContentFetcher(HttpClient.newHttpClient(), 3, Duration.ofMillis(1));

    assertThrows(WebContentRetrievalException.class, () -> fetcher.fetch(url));
    assertEquals(3, requests.get());
  }

  @Test
  void retriesOnNetworkFailureAndSucceeds() {
    byte[] body = "recovered".getBytes(StandardCharsets.UTF_8);
    AtomicInteger attempts = new AtomicInteger();
    HttpContentFetcher fetcher =
        new HttpContentFetcher(HttpClient.newHttpClient(), 3, Duration.ofMillis(1)) {
          @Override
          byte[] send(String url) throws IOException {
            if (attempts.incrementAndGet() < 3) {
              throw new IOException("simulated connection failure");
            }
            return body;
          }
        };

    byte[] content = fetcher.fetch("http://example.invalid/content");

    assertEquals(3, attempts.get());
    assertEquals("recovered", new String(content, StandardCharsets.UTF_8));
  }

  @Test
  void failsAfterExhaustingRetries() {
    AtomicInteger attempts = new AtomicInteger();
    HttpContentFetcher fetcher =
        new HttpContentFetcher(HttpClient.newHttpClient(), 3, Duration.ofMillis(1)) {
          @Override
          byte[] send(String url) throws IOException {
            attempts.incrementAndGet();
            throw new IOException("simulated connection failure");
          }
        };

    WebContentRetrievalException exception =
        assertThrows(
            WebContentRetrievalException.class,
            () -> fetcher.fetch("http://example.invalid/content"));

    assertEquals(3, attempts.get());
    assertInstanceOf(IOException.class, exception.getCause());
  }
}
