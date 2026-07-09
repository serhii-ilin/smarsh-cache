package com.smarsh.retriever.service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.logging.Logger;

public class CachedWebContentRetriever implements ContentRetriever {
    private static final Logger logger = Logger.getLogger(CachedWebContentRetriever.class.getCanonicalName());

    private final HttpContentFetcher httpContentFetcher;

    public CachedWebContentRetriever() {
        this(new HttpContentFetcher());
    }

    CachedWebContentRetriever(HttpContentFetcher httpContentFetcher) {
        this.httpContentFetcher = httpContentFetcher;
    }

    @Override
    public Response retrieve(Request request) {
        Objects.requireNonNull(request, "request is required");
        logger.info("Retrieving cached content for URL: " + request.getUrl());
        byte[] content = httpContentFetcher.fetch(request.getUrl());
        return new Response(request.getUrl(), content, OffsetDateTime.now());
    }
}
