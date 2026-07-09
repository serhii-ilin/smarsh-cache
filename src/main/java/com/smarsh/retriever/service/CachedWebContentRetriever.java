package com.smarsh.retriever.service;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.logging.Logger;

public class CachedWebContentRetriever implements ContentRetriever {
    private static final Logger logger = Logger.getLogger(CachedWebContentRetriever.class.getCanonicalName());
    @Override
    public Response retrieve(Request request) {
        Objects.requireNonNull(request, "request is required");
        logger.info("Retrieving cached content for URL: " + request.getUrl());
        return new Response(request.getUrl(), new byte[0], OffsetDateTime.now());
    }
}
