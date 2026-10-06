package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * "I felt this too" on a story. The story feature owns the rules (story must be visible,
 * can't resonate with your own); the resonance feature just stores and counts.
 */
@Service
public class StoryResonanceService {

    static final int RESONATOR_LIMIT = 50;

    private final StoryRepository stories;
    private final ResonanceService resonances;
    private final UserService users;

    public StoryResonanceService(StoryRepository stories, ResonanceService resonances, UserService users) {
        this.stories = stories;
        this.resonances = resonances;
        this.users = users;
    }

    @Transactional
    public ResonanceSummary resonate(String authUid, long storyId) {
        long userId = requireProfile(authUid);
        Story story = requireVisible(storyId);
        if (story.isWrittenBy(userId)) throw ApiErrors.badRequest("That's your own story.");
        return resonances.add(userId, storyId);
    }

    /** Always allowed, even on your own story or one that was since hidden: undo should never fail. */
    @Transactional
    public ResonanceSummary unresonate(String authUid, long storyId) {
        return resonances.remove(requireProfile(authUid), storyId);
    }

    /** The people who felt it too, newest first. This is how you find "your people". */
    @Transactional(readOnly = true)
    public List<Author> resonators(long storyId) {
        requireVisible(storyId);
        List<Long> ids = resonances.recentResonatorIds(storyId, RESONATOR_LIMIT);
        Map<Long, Author> authors = users.findAuthors(ids);
        return ids.stream().map(authors::get).filter(Objects::nonNull).toList();   // keep newest-first order
    }

    private long requireProfile(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    private Story requireVisible(long storyId) {
        return stories.findById(storyId)
                .filter(s -> s.getStatus() == StoryStatus.VISIBLE)
                .orElseThrow(() -> ApiErrors.notFound("Story not found"));
    }
}
