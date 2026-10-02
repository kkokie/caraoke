package com.caraoke.song;

import com.caraoke.common.ApiErrors;
import com.caraoke.song.SongDtos.SearchResult;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.catalog.CatalogClient;
import com.caraoke.song.catalog.CatalogTrack;
import com.caraoke.song.catalog.CatalogUnavailableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * Public entry point to the song feature. Other features (stories, resonance) call this,
 * never SongRepository directly.
 */
@Service
public class SongService {

    static final int MAX_QUERY_LENGTH = 100;
    static final int SEARCH_LIMIT = 25;

    private final SongRepository songs;
    private final CatalogClient catalog;

    public SongService(SongRepository songs, CatalogClient catalog) {
        this.songs = songs;
        this.catalog = catalog;
    }

    public List<SearchResult> search(String rawQuery) {
        String query = requireQuery(rawQuery);
        return fromCatalog(() -> catalog.search(query, SEARCH_LIMIT)).stream()
                .map(SearchResult::of)
                .toList();
    }

    /**
     * Turn a catalog hit into one of our songs (create on first open, reuse after).
     * Metadata comes from the catalog, not the client, so nobody can plant a fake song.
     * Deliberately not @Transactional: the network call shouldn't hold a DB transaction open.
     */
    public SongView resolve(String appleId) {
        Song song = songs.findByAppleId(appleId).orElseGet(() -> importFromCatalog(appleId));
        return SongView.of(song);
    }

    public SongView get(long id) {
        return songs.findById(id)
                .map(SongView::of)
                .orElseThrow(() -> ApiErrors.notFound("Song not found"));
    }

    private Song importFromCatalog(String appleId) {
        CatalogTrack track = fromCatalog(() -> catalog.lookup(appleId))
                .orElseThrow(() -> ApiErrors.notFound("Song not found in catalog"));
        try {
            return songs.saveAndFlush(Song.from(track));
        } catch (DataIntegrityViolationException race) {
            // Two people opened the same new song at once; the unique apple_id kept one row
            return songs.findByAppleId(appleId).orElseThrow(() -> race);
        }
    }

    private static <T> T fromCatalog(Supplier<T> call) {
        try {
            return call.get();
        } catch (CatalogUnavailableException e) {
            throw ApiErrors.badGateway("Music search is unavailable right now. Try again in a moment.", e);
        }
    }

    private static String requireQuery(String raw) {
        String q = raw == null ? "" : raw.trim();
        if (q.isEmpty()) throw ApiErrors.badRequest("Search for a song or artist.");
        if (q.length() > MAX_QUERY_LENGTH) throw ApiErrors.badRequest("Search is too long.");
        return q;
    }
}
