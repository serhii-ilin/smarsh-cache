package com.smarsh.retriever.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CachedWebContentRetrieverTest {
    @Test
    void requestIsRequired() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new CachedWebContentRetriever().retrieve(null));
        assertEquals("request is required", exception.getMessage());
    }
}
