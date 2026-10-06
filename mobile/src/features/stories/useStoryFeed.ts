import { useCallback, useRef, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { api, Story } from '@/api/client';

type FeedState = {
  status: 'loading' | 'ready' | 'error';
  items: Story[];
  nextCursor: number | null;
  loadingMore: boolean;
  error: string | null;
};

const INITIAL: FeedState = { status: 'loading', items: [], nextCursor: null, loadingMore: false, error: null };

/**
 * A song's story feed. Reloads whenever the song page regains focus,
 * so a story you just posted from the composer shows up on return.
 */
export function useStoryFeed(songId: number) {
  const [state, setState] = useState<FeedState>(INITIAL);
  const loadingMoreRef = useRef(false);   // guards against double "load more" taps

  const refresh = useCallback(async () => {
    try {
      const page = await api.getStories(songId);
      setState({ status: 'ready', items: page.items, nextCursor: page.nextCursor, loadingMore: false, error: null });
    } catch (e) {
      setState((s) => ({ ...s, status: 'error', error: message(e) }));
    }
  }, [songId]);

  useFocusEffect(
    useCallback(() => {
      refresh();
    }, [refresh]),
  );

  async function loadMore() {
    if (!state.nextCursor || loadingMoreRef.current) return;
    loadingMoreRef.current = true;
    setState((s) => ({ ...s, loadingMore: true }));
    try {
      const page = await api.getStories(songId, state.nextCursor);
      setState((s) => ({ ...s, items: [...s.items, ...page.items], nextCursor: page.nextCursor, loadingMore: false }));
    } catch (e) {
      setState((s) => ({ ...s, loadingMore: false, error: message(e) }));
    } finally {
      loadingMoreRef.current = false;
    }
  }

  function removeLocally(id: number) {
    setState((s) => ({ ...s, items: s.items.filter((story) => story.id !== id) }));
  }

  return { ...state, refresh, loadMore, removeLocally };
}

function message(e: unknown) {
  return e instanceof Error ? e.message : String(e);
}
