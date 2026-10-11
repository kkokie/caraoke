package com.caraoke.song;

import com.caraoke.song.catalog.CatalogTrack;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Re-ranks catalog search results toward the song people most likely mean.
 * The iTunes Search API sorts by text relevance and gives no play counts, so "mask off" can put
 * soundalikes above Future's original. We fix the common cases with simple, explainable signals:
 * exact titles, the artist named in the query, the original release over covers/karaoke/remixes,
 * and songs people already opened on caraoke. Apple Music's catalog search (popularity-aware) is
 * the real upgrade once we have MusicKit.
 */
final class SearchRanker {

    // Versions people rarely mean when they type a title
    private static final Pattern NOT_THE_SONG = Pattern.compile(
            "karaoke|in the style of|originally performed|made famous|tribute|lullab|8-bit|8 bit|nightcore"
                    + "|sped up|slowed|reverb|\\bcover\\b|instrumental|piano version|backing track");
    private static final Pattern ALT_VERSION = Pattern.compile("\\bremix\\b|\\brmx\\b|\\bbootleg\\b|\\bvip\\b|\\bflip\\b");
    private static final Pattern LIVE = Pattern.compile("\\blive\\b");
    // "(feat. X)", "[Remastered]", " - Radio Edit": decoration around the real title
    private static final Pattern DECORATION = Pattern.compile("\\s*[(\\[].*?[)\\]]|\\s+-\\s+.*$");

    // Weights. ORIGINAL > PRIOR_WEIGHT so the original release wins however low iTunes put it;
    // naming the artist (+3) still beats ORIGINAL, so "mask off elior norlander" gets Elior's.
    static final double PRIOR_WEIGHT = 1.5;
    static final double ORIGINAL = 2.5;
    static final double OPENED_HERE = 1.0;

    private SearchRanker() { }

    static List<CatalogTrack> rank(String query, List<CatalogTrack> tracks, Set<String> openedOnCaraoke, int limit) {
        String q = normalize(query);
        Set<String> qWords = words(q);
        Map<String, LocalDate> firstRelease = firstReleasePerTitle(tracks);

        List<Scored> scored = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            CatalogTrack t = tracks.get(i);
            double prior = PRIOR_WEIGHT * (1.0 - (double) i / Math.max(1, tracks.size()));   // keep some of iTunes' order
            scored.add(new Scored(t, prior + score(t, q, qWords, firstRelease, openedOnCaraoke), i));
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed().thenComparingInt(Scored::position));

        // The same recording often appears several times (single, album, deluxe): keep the best copy.
        // Keyed on the full title, so a remix or live version stays as its own result.
        Set<String> seen = new HashSet<>();
        List<CatalogTrack> out = new ArrayList<>();
        for (Scored s : scored) {
            if (seen.add(normalize(s.track().title()) + "|" + normalize(s.track().artist()))) out.add(s.track());
            if (out.size() == limit) break;
        }
        return out;
    }

    private static double score(CatalogTrack t, String q, Set<String> qWords,
                                Map<String, LocalDate> firstRelease, Set<String> opened) {
        String title = baseTitle(t.title());
        String everything = normalize(t.title() + " " + t.album() + " " + t.artist());
        Set<String> artistWords = words(normalize(t.artist()));
        double s = 0;

        if (title.equals(q)) s += 3;                                   // "mask off" == "Mask Off"
        else if (title.startsWith(q)) s += 1.5;
        if (!Collections.disjoint(qWords, artistWords)
                && words(title + " " + normalize(t.artist())).containsAll(qWords)) s += 3;   // "mask off future"

        if (NOT_THE_SONG.matcher(everything).find()) s -= 4;
        else if (ALT_VERSION.matcher(normalize(t.title())).find()) s -= 1.5;
        if (LIVE.matcher(normalize(t.title())).find()) s -= 0.5;
        if (t.compilation()) s -= 1;

        LocalDate first = firstRelease.get(title);
        if (first != null && first.equals(t.releaseDate())) s += ORIGINAL;   // the original came out first
        if (opened.contains(t.appleId())) s += OPENED_HERE;            // people here already went looking for it
        return s;
    }

    private static Map<String, LocalDate> firstReleasePerTitle(List<CatalogTrack> tracks) {
        Map<String, LocalDate> first = new HashMap<>();
        for (CatalogTrack t : tracks) {
            if (t.releaseDate() == null || NOT_THE_SONG.matcher(normalize(t.title() + " " + t.album())).find()) continue;
            first.merge(baseTitle(t.title()), t.releaseDate(), (a, b) -> a.isBefore(b) ? a : b);
        }
        return first;
    }

    static String baseTitle(String title) {
        return normalize(DECORATION.matcher(title == null ? "" : title).replaceAll(""));
    }

    static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}' ]+", " ").replaceAll("\\s+", " ").strip();
    }

    private static Set<String> words(String s) {
        return s.isBlank() ? Set.of() : new HashSet<>(Arrays.asList(s.split(" ")));
    }

    private record Scored(CatalogTrack track, double score, int position) { }
}
