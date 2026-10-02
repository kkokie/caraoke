package com.caraoke.song.catalog;

/** The external catalog is down, slow, or returned something we can't read. */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
