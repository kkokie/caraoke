package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns Story rows into what the app shows: author info + resonance counts.
 * Batch lookups, so a page costs a fixed number of queries no matter how many stories.
 */
@Component
class StoryViewAssembler {

    private final UserService users;
    private final ResonanceService resonances;

    StoryViewAssembler(UserService users, ResonanceService resonances) {
        this.users = users;
        this.resonances = resonances;
    }

    List<StoryView> toViews(List<Story> stories, Long viewerId) {
        if (stories.isEmpty()) return List.of();
        Map<Long, Author> authors = users.findAuthors(authorIds(stories));
        Map<Long, ResonanceSummary> felt = resonances.summaries(storyIds(stories), viewerId);
        return stories.stream()
                .map(s -> StoryView.of(
                        s,
                        authors.get(s.getUserId()),
                        viewerId != null && s.isWrittenBy(viewerId),
                        felt.getOrDefault(s.getId(), ResonanceSummary.NONE)))
                .toList();
    }

    StoryView toView(Story story, Long viewerId) {
        return toViews(List.of(story), viewerId).get(0);
    }

    private static Set<Long> authorIds(List<Story> stories) {
        return stories.stream().map(Story::getUserId).collect(Collectors.toSet());
    }

    private static List<Long> storyIds(List<Story> stories) {
        return stories.stream().map(Story::getId).toList();
    }
}
