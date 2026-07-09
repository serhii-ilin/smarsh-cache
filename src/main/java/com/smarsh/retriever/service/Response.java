package com.smarsh.retriever.service;

import java.time.OffsetDateTime;

public record Response(String url, byte[] content, OffsetDateTime createdAt) {}
