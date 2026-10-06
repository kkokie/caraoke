import { useCallback, useRef, useState } from 'react';
import { api, StoryTile } from '@/api/client';

type FeltState = {
  status: 'idle' | 'loading' | 'ready' | 'error';
  tiles: StoryTile[];
  nextCursor: string | null;
  loadingMore: boolean;
};

const INITIAL: FeltState = { status: 'idle', tiles: [], nextCursor: null, loadingMore: false };

/** Your private "Felt" grid: stories you resonated with, newest first. Loads on demand. */
export function useFeltTiles() {
  const [state, setState] = useState<FeltState>(INITIAL);
  const loadingMoreRef = useRef(false);

  const refresh = useCallback(async () => {
    setState((s) => (s.status === 'idle' ? { ...s, status: 'loading' } : s));
    try {
      const page = await api.getMyFelt();
      setState({ status: 'ready', tiles: page.items, nextCursor: page.nextCursor, loadingMore: false });
    } catch {
      setState((s) => ({ ...s, status: 'error' }));
    }
  }, []);

  async function loadMore() {
    if (!state.nextCursor || loadingMoreRef.current) return;
    loadingMoreRef.current = true;
    setState((s) => ({ ...s, loadingMore: true }));
    try {
      const page = await api.getMyFelt(state.nextCursor);
      setState((s) => ({ ...s, tiles: [...s.tiles, ...page.items], nextCursor: page.nextCursor, loadingMore: false }));
    } catch {
      setState((s) => ({ ...s, loadingMore: false }));
    } finally {
      loadingMoreRef.current = false;
    }
  }

  return { ...state, refresh, loadMore };
}
