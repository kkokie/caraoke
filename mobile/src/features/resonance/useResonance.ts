import { useEffect, useRef, useState } from 'react';
import * as Haptics from 'expo-haptics';
import { api, ResonanceSummary, Story } from '@/api/client';

/**
 * Optimistic "I felt this too":
 * - the heart flips instantly (plus a light haptic tap),
 * - the server call runs in the background,
 * - rapid double-taps settle on whatever the user tapped LAST,
 * - on failure we roll back to the last state the server confirmed.
 */
export function useResonance(story: Story) {
  const confirmed = useRef<ResonanceSummary>({ count: story.resonanceCount, mine: story.resonatedByMe });
  const wanted = useRef(story.resonatedByMe);
  const syncing = useRef(false);
  const [shown, setShown] = useState<ResonanceSummary>(confirmed.current);

  // A feed refresh brings fresh numbers from the server
  useEffect(() => {
    confirmed.current = { count: story.resonanceCount, mine: story.resonatedByMe };
    wanted.current = story.resonatedByMe;
    setShown(confirmed.current);
  }, [story.id, story.resonanceCount, story.resonatedByMe]);

  function toggle() {
    if (story.mine) return;                       // can't resonate with your own story
    const next = !wanted.current;
    wanted.current = next;
    setShown((s) => ({ count: Math.max(0, s.count + (next ? 1 : -1)), mine: next }));
    if (next) Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light).catch(() => {});
    sync();
  }

  async function sync() {
    if (syncing.current) return;                  // the running loop will pick up the latest intent
    syncing.current = true;
    try {
      while (confirmed.current.mine !== wanted.current) {
        const target = wanted.current;
        confirmed.current = target ? await api.resonate(story.id) : await api.unresonate(story.id);
      }
      setShown(confirmed.current);                // settle on the server's real count
    } catch {
      wanted.current = confirmed.current.mine;    // roll back quietly
      setShown(confirmed.current);
    } finally {
      syncing.current = false;
    }
  }

  return { count: shown.count, mine: shown.mine, toggle };
}
