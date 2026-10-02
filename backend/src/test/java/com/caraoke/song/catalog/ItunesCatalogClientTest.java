package com.caraoke.song.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItunesCatalogClientTest {

    // What iTunes really sends back; Spring's JSON converter won't take it, hence the manual parse
    private static final MediaType ITUNES_TYPE = MediaType.parseMediaType("text/javascript;charset=utf-8");

    private static final String SEARCH_JSON = """
            {"resultCount":2,"results":[
              {"wrapperType":"track","kind":"song","trackId":1440744581,
               "trackName":"Mr. Brightside","artistName":"The Killers","collectionName":"Hot Fuss",
               "artworkUrl100":"https://is1-ssl.mzstatic.com/image/thumb/abc/100x100bb.jpg",
               "trackTimeMillis":222973,"someFieldWeIgnore":true},
              {"wrapperType":"track","kind":"music-video","trackId":999,
               "trackName":"Mr. Brightside (Video)","artistName":"The Killers"}
            ]}
            """;

    private static final String EMPTY_JSON = """
            {"resultCount":0,"results":[]}
            """;

    private MockRestServiceServer server;
    private ItunesCatalogClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ItunesCatalogClient(builder, new ObjectMapper(), "https://itunes.test", "us");
    }

    @Test
    void searchMapsSongsAndSkipsNonSongs() {
        server.expect(requestTo(startsWith("https://itunes.test/search?")))
                .andExpect(queryParam("entity", "song"))
                .andExpect(queryParam("limit", "25"))
                .andRespond(withSuccess(SEARCH_JSON, ITUNES_TYPE));

        List<CatalogTrack> tracks = client.search("mr brightside", 25);

        assertThat(tracks).hasSize(1);
        CatalogTrack t = tracks.get(0);
        assertThat(t.appleId()).isEqualTo("1440744581");
        assertThat(t.title()).isEqualTo("Mr. Brightside");
        assertThat(t.artist()).isEqualTo("The Killers");
        assertThat(t.album()).isEqualTo("Hot Fuss");
        assertThat(t.artworkUrl()).endsWith("/600x600bb.jpg");   // upscaled from 100x100
        assertThat(t.durationSec()).isEqualTo(223);              // 222.973s rounded
        server.verify();
    }

    @Test
    void lookupReturnsTheSong() {
        server.expect(requestTo(startsWith("https://itunes.test/lookup?")))
                .andExpect(queryParam("id", "1440744581"))
                .andRespond(withSuccess(SEARCH_JSON, ITUNES_TYPE));

        assertThat(client.lookup("1440744581"))
                .map(CatalogTrack::title)
                .contains("Mr. Brightside");
    }

    @Test
    void lookupOfUnknownIdIsEmpty() {
        server.expect(requestTo(startsWith("https://itunes.test/lookup?")))
                .andRespond(withSuccess(EMPTY_JSON, ITUNES_TYPE));

        assertThat(client.lookup("1")).isEmpty();
    }

    @Test
    void serverErrorBecomesCatalogUnavailable() {
        server.expect(requestTo(startsWith("https://itunes.test/search?")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.search("x", 25)).isInstanceOf(CatalogUnavailableException.class);
    }

    @Test
    void garbageBodyBecomesCatalogUnavailable() {
        server.expect(requestTo(startsWith("https://itunes.test/search?")))
                .andRespond(withSuccess("<html>rate limited</html>", ITUNES_TYPE));

        assertThatThrownBy(() -> client.search("x", 25)).isInstanceOf(CatalogUnavailableException.class);
    }
}
