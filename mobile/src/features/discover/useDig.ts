import { useCallback, useEffect, useRef, useState } from 'react';
import { api, FoundStory } from '@/api/client';

export type DigState =
  | { kind: 'digging' }
  | { kind: 'found'; found: FoundStory }
  | { kind: 'empty' }                 // nothing (left) to find
  | { kind: 'error'; message: string };

/**
 * "A story you found" + Dig again. Remembers what you've dug up this session so the next
 * dig is always new; when everything's been seen, it says so instead of repeating.
 */
export function useDig() {
  const [state, setState] = useState<DigState>({ kind: 'digging' });
  const seen = useRef<number[]>([]);

  const dig = useCallback(async () => {
    setState({ kind: 'digging' });
    try {
      const found = await api.dig(seen.current);
      if (!found) return setState({ kind: 'empty' });
      seen.current = [...seen.current, found.story.id];
      setState({ kind: 'found', found });
    } catch (e) {
      setState({ kind: 'error', message: e instanceof Error ? e.message : 'Couldn’t dig right now' });
    }
  }, []);

  /** Start over (e.g. after "you've seen everything"), so old favorites can turn up again. */
  const startOver = useCallback(() => {
    seen.current = [];
    dig();
  }, [dig]);

  useEffect(() => {
    dig();
  }, [dig]);

  return { state, dig, startOver, hasSeenAny: () => seen.current.length > 0 };
}
