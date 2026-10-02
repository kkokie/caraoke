package com.caraoke.song.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * iTunes Search API: free, no key, ~20 requests/minute per server IP.
 * Docs: https://performance-partners.apple.com/search-api
 */
@Component
public class ItunesCatalogClient implements CatalogClient {

    private final RestClient http;
    private final ObjectMapper json;
    private final String country;

    public ItunesCatalogClient(RestClient.Builder builder,
                               ObjectMapper json,
                               @Value("${app.catalog.itunes.base-url:https://itunes.apple.com}") String baseUrl,
                               @Value("${app.catalog.itunes.country:us}") String country) {
        this.http = builder.baseUrl(baseUrl).build();
        this.json = json;
        this.country = country;
    }

    @Override
    public List<CatalogTrack> search(String query, int limit) {
        return fetch(uri -> uri.path("/search")
                .queryParam("term", query)
                .queryParam("media", "music")
                .queryParam("entity", "song")
                .queryParam("limit", limit)
                .queryParam("country", country)
                .build());
    }

    @Override
    public Optional<CatalogTrack> lookup(String appleId) {
        return fetch(uri -> uri.path("/lookup")
                .queryParam("id", appleId)
                .queryParam("country", country)
                .build())
                .stream()
                .findFirst();
    }

    private List<CatalogTrack> fetch(Function<UriBuilder, URI> uri) {
        ItunesPayload.Response response = parse(get(uri));
        if (response.results() == null) return List.of();
        return response.results().stream()
                .filter(ItunesPayload.Track::isSong)
                .map(ItunesMapper::toTrack)
                .toList();
    }

    // iTunes answers with Content-Type "text/javascript", which Spring's JSON converter
    // won't accept, so read the raw body and parse it ourselves.
    private String get(Function<UriBuilder, URI> uri) {
        try {
            return http.get().uri(uri).retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new CatalogUnavailableException("iTunes request failed", e);
        }
    }

    private ItunesPayload.Response parse(String body) {
        if (body == null || body.isBlank()) return new ItunesPayload.Response(List.of());
        try {
            return json.readValue(body, ItunesPayload.Response.class);
        } catch (JsonProcessingException e) {
            throw new CatalogUnavailableException("iTunes returned unreadable JSON", e);
        }
    }
}
