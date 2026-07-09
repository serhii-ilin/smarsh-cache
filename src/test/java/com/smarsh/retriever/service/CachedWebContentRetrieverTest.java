package com.smarsh.retriever.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CachedWebContentRetrieverTest {

    @Test
    void requestIsRequired() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new CachedWebContentRetriever().retrieve(null));
        assertEquals("request is required", exception.getMessage());
    }

    @Test
    void delegatesToHttpContentFetcherAndWrapsResponse() {
        String url = "http://example.invalid/content";
        byte[] body = "hello world".getBytes(StandardCharsets.UTF_8);
        HttpContentFetcher fetcher = new HttpContentFetcher() {
            @Override
            public byte[] fetch(String requestedUrl) {
                assertEquals(url, requestedUrl);
                return body;
            }
        };
        CachedWebContentRetriever retriever = new CachedWebContentRetriever(fetcher);

        Response response = retriever.retrieve(Request.of(url));

        assertEquals(url, response.url());
        assertEquals("hello world", new String(response.content(), StandardCharsets.UTF_8));
        assertNotNull(response.createdAt());
    }
}
