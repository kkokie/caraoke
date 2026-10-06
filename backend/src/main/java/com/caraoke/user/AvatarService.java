package com.caraoke.user;

import com.caraoke.common.ApiErrors;
import com.caraoke.media.ImageRules;
import com.caraoke.media.ImageRules.ImageType;
import com.caraoke.media.MediaCleanup;
import com.caraoke.media.MediaStorage;
import com.caraoke.user.UserDtos.PublicProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Profile photo: upload (replacing any old one) and remove. */
@Service
public class AvatarService {

    private final UserRepository users;
    private final MediaStorage media;

    public AvatarService(UserRepository users, MediaStorage media) {
        this.users = users;
        this.media = media;
    }

    @Transactional
    public PublicProfile set(String authUid, byte[] bytes) {
        User user = requireUser(authUid);
        ImageType type = ImageRules.validate(bytes);                   // 400 for non-images / too big
        String key = "avatars/" + user.getId() + "/" + UUID.randomUUID() + type.extension();
        media.put(key, bytes, type.contentType());
        replaceKey(user, key);
        return PublicProfile.of(user, media);
    }

    @Transactional
    public PublicProfile remove(String authUid) {
        User user = requireUser(authUid);
        replaceKey(user, null);
        return PublicProfile.of(user, media);
    }

    /** A fresh key per upload means caches never serve a stale photo; the old file is removed after commit. */
    private void replaceKey(User user, String newKey) {
        String old = user.getAvatarKey();
        user.setAvatarKey(newKey);
        MediaCleanup.deleteAfterCommit(media, old);
    }

    private User requireUser(String authUid) {
        return users.findByAuthUid(authUid).orElseThrow(() -> ApiErrors.notFound("Profile not created yet"));
    }
}
