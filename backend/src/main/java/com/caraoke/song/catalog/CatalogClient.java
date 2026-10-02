package com.caraoke.song.catalog;

import java.util.List;
import java.util.Optional;

/**
 * Where song metadata comes from. Today: iTunes Search API. Later: Apple MusicKit.
 * Nothing outside this package should know which one is behind it.
 */
public interface CatalogClient {

    List<CatalogTrack> search(String query, int limit);

    Optional<CatalogTrack> lookup(String appleId);
}
