package com.smarsh.retriever.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        server.createContext("/content", exchange -> {
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
    void retriesOnNetworkFailureAndSucceeds() {
        byte[] body = "recovered".getBytes(StandardCharsets.UTF_8);
        AtomicInteger attempts = new AtomicInteger();
        HttpContentFetcher fetcher = new HttpContentFetcher(HttpClient.newHttpClient(), 3, Duration.ofMillis(1)) {
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
        HttpContentFetcher fetcher = new HttpContentFetcher(HttpClient.newHttpClient(), 3, Duration.ofMillis(1)) {
            @Override
            byte[] send(String url) throws IOException {
                attempts.incrementAndGet();
                throw new IOException("simulated connection failure");
            }
        };

        WebContentRetrievalException exception = assertThrows(WebContentRetrievalException.class,
                () -> fetcher.fetch("http://example.invalid/content"));

        assertEquals(3, attempts.get());
        assertInstanceOf(IOException.class, exception.getCause());
    }
}
