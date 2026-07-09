package com.smarsh.retriever.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

public class HttpContentFetcher {
    private static final Logger logger = Logger.getLogger(HttpContentFetcher.class.getCanonicalName());
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(200);

    private final HttpClient httpClient;
    private final int maxAttempts;
    private final Duration retryDelay;

    public HttpContentFetcher() {
        this(HttpClient.newHttpClient(), DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    HttpContentFetcher(HttpClient httpClient) {
        this(httpClient, DEFAULT_MAX_ATTEMPTS, DEFAULT_RETRY_DELAY);
    }

    HttpContentFetcher(HttpClient httpClient, int maxAttempts, Duration retryDelay) {
        this.httpClient = httpClient;
        this.maxAttempts = maxAttempts;
        this.retryDelay = retryDelay;
    }

    public byte[] fetch(String url) {
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return send(url);
            } catch (IOException e) {
                lastFailure = e;
                logger.warning("Attempt " + attempt + "/" + maxAttempts + " failed to retrieve content for URL: "
                        + url + " - " + e.getMessage());
                if (attempt < maxAttempts) {
                    sleepBeforeRetry();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WebContentRetrievalException("Interrupted while retrieving content for URL: " + url, e);
            }
        }
        throw new WebContentRetrievalException(
                "Failed to retrieve content for URL: " + url + " after " + maxAttempts + " attempts", lastFailure);
    }

    byte[] send(String url) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        return httpResponse.body();
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebContentRetrievalException("Interrupted while waiting to retry", e);
        }
    }
}
