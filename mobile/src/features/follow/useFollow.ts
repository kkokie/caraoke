import { useEffect, useRef, useState } from 'react';
import * as Haptics from 'expo-haptics';
import { api, FollowState, ProfileView } from '@/api/client';

/**
 * Optimistic Follow button (same pattern as "I felt this too"):
 * flips instantly, syncs in the background, settles on the LAST tap, rolls back on failure.
 */
export function useFollow(profile: ProfileView) {
  const handle = profile.user.handle;
  const confirmed = useRef<FollowState>({ following: profile.social.followedByMe, followers: profile.social.followers });
  const wanted = useRef(profile.social.followedByMe);
  const syncing = useRef(false);
  const [shown, setShown] = useState<FollowState>(confirmed.current);

  // A profile refresh brings fresh numbers
  useEffect(() => {
    confirmed.current = { following: profile.social.followedByMe, followers: profile.social.followers };
    wanted.current = profile.social.followedByMe;
    setShown(confirmed.current);
  }, [handle, profile.social.followedByMe, profile.social.followers]);

  function toggle() {
    if (profile.me) return;
    const next = !wanted.current;
    wanted.current = next;
    setShown((s) => ({ following: next, followers: Math.max(0, s.followers + (next ? 1 : -1)) }));
    if (next) Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light).catch(() => {});
    sync();
  }

  async function sync() {
    if (syncing.current) return;
    syncing.current = true;
    try {
      while (confirmed.current.following !== wanted.current) {
        confirmed.current = wanted.current ? await api.follow(handle) : await api.unfollow(handle);
      }
      setShown(confirmed.current);
    } catch {
      wanted.current = confirmed.current.following;
      setShown(confirmed.current);
    } finally {
      syncing.current = false;
    }
  }

  return { following: shown.following, followers: shown.followers, toggle };
}
